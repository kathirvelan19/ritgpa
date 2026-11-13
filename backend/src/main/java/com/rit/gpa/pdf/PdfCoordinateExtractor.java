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
        if (textPositions != null && !textPositions.isEmpty() && text != null && !text.trim().isEmpty()) {
            TextPosition first = textPositions.get(0);
            TextPosition last = textPositions.get(textPositions.size() - 1);
            float startX = first.getXDirAdj();
            float endX = last.getXDirAdj() + last.getWidthDirAdj();
            float width = Math.max(endX - startX, first.getWidthDirAdj());
            float y = first.getYDirAdj();
            float height = first.getHeightDir();

            fragments.add(new TextFragment(text.trim(), startX, y, width, height));
        }
        super.writeString(text, textPositions);
    }

    public List<TextFragment> getFragments() {
        return Collections.unmodifiableList(fragments);
    }

    /**
     * Groups text fragments into horizontal lines by matching y-coordinates within tolerance.
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
                // If fragment overlaps column range
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

