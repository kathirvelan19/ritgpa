package com.rit.gpa.pdf;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PdfCoordinateExtractor extends PDFTextStripper {
    private final List<TextFragment> fragments = new ArrayList<>();

    public PdfCoordinateExtractor() throws IOException {
        super();
        setSortByPosition(true);
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        if (textPositions == null || textPositions.isEmpty()) {
            super.writeString(text, textPositions);
            return;
        }

        // Group text positions into horizontal column segments by detecting inter-column spacing gaps
        List<TextPosition> currentChunk = new ArrayList<>();

        for (TextPosition tp : textPositions) {
            if (currentChunk.isEmpty()) {
                currentChunk.add(tp);
            } else {
                TextPosition prev = currentChunk.get(currentChunk.size() - 1);
                float gap = tp.getXDirAdj() - (prev.getXDirAdj() + prev.getWidthDirAdj());
                float yDiff = Math.abs(tp.getYDirAdj() - prev.getYDirAdj());

                // If horizontal gap between characters exceeds column gap threshold (12 pt) or y jumps
                if (gap > 12.0f || yDiff > 3.0f) {
                    flushChunk(currentChunk);
                    currentChunk.clear();
                }
                currentChunk.add(tp);
            }
        }

        if (!currentChunk.isEmpty()) {
            flushChunk(currentChunk);
        }

        super.writeString(text, textPositions);
    }

    private void flushChunk(List<TextPosition> chunk) {
        if (chunk.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (TextPosition tp : chunk) {
            sb.append(tp.getUnicode());
        }

        String str = sb.toString().replace('\u00a0', ' ').trim();
        if (str.isEmpty()) return;

        TextPosition first = chunk.get(0);
        TextPosition last = chunk.get(chunk.size() - 1);
        float startX = first.getXDirAdj();
        float endX = last.getXDirAdj() + last.getWidthDirAdj();
        float width = Math.max(endX - startX, first.getWidthDirAdj());
        float y = first.getYDirAdj();
        float height = first.getHeightDir();

        fragments.add(new TextFragment(str, startX, y, width, height));
    }

    public List<TextFragment> getFragments() {
        return Collections.unmodifiableList(fragments);
    }

    /**
     * Groups text fragments into rows based on Y-coordinate proximity within tolerance,
     * sorting fragments within each row from left to right.
     */
    public List<PdfLine> getLines(float yTolerance) {
        List<TextFragment> sorted = new ArrayList<>(fragments);
        sorted.sort(Comparator.comparingDouble(TextFragment::getY).thenComparingDouble(TextFragment::getX));

        List<PdfLine> lines = new ArrayList<>();
        PdfLine currentLine = null;

        for (TextFragment fragment : sorted) {
            if (currentLine == null || Math.abs(fragment.getY() - currentLine.getBaseY()) > yTolerance) {
                currentLine = new PdfLine(fragment.getY());
                lines.add(currentLine);
            }
            currentLine.addFragment(fragment);
        }

        return lines;
    }

    public static class PdfLine {
        private final float baseY;
        private final List<TextFragment> lineFragments = new ArrayList<>();

        public PdfLine(float baseY) {
            this.baseY = baseY;
        }

        public void addFragment(TextFragment fragment) {
            lineFragments.add(fragment);
            lineFragments.sort(Comparator.comparingDouble(TextFragment::getX));
        }

        public float getBaseY() {
            return baseY;
        }

        public List<TextFragment> getLineFragments() {
            return lineFragments;
        }

        public String getFullText() {
            StringBuilder sb = new StringBuilder();
            for (TextFragment tf : lineFragments) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(tf.getText());
            }
            return sb.toString();
        }

        public String getTextInColumn(double minX, double maxX) {
            StringBuilder sb = new StringBuilder();
            for (TextFragment tf : lineFragments) {
                double fragCenterX = tf.getX() + (tf.getWidth() / 2.0);
                if (fragCenterX >= minX && fragCenterX <= maxX) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(tf.getText());
                }
            }
            return sb.toString().trim();
        }
    }
}
