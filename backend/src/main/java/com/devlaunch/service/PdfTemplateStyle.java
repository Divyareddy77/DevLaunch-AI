package com.devlaunch.service;

import java.awt.Color;

/**
 * Visual configuration for a {@link ResumePdfTemplate}.
 * <p>
 * Every tunable presentation detail lives here so the single PDF
 * renderer only switches on these values. Beyond colours, typography,
 * and spacing, the record also carries the layout-level choices that
 * make each template genuinely distinct: the header arrangement
 * ({@link HeaderLayout}), a two-column contact grid, a short accent bar
 * above section headings, entry titles tinted with the heading colour,
 * a highlighted skills box, page margins, and body line spacing.
 * </p>
 *
 * @param headingColor         colour of section headings and the header band
 * @param textColor            colour of body text and the candidate's name
 * @param mutedColor           colour of dates, metadata, and contact details
 * @param ruleColor            colour of section divider lines
 * @param bandTextColor        text colour inside the coloured header band
 * @param bandMutedColor       muted text colour inside the coloured header band
 * @param highlightFill        background fill of the highlighted skills box
 * @param headerLayout         how the name/headline/contact header is arranged
 * @param contactColumns       whether contact details render as a two-column grid
 * @param uppercaseName        whether the name renders in upper case
 * @param headingBar           whether a short accent bar renders above headings
 * @param coloredEntryTitles   whether entry titles use the heading colour
 * @param skillsHighlight      whether the skills section renders in a box
 * @param nameSize             font size of the candidate's name
 * @param sectionSize          font size of section headings
 * @param bodySize             font size of body text
 * @param metaSize             font size of secondary text (dates, metadata)
 * @param margin               page margin in points (top/bottom slightly larger)
 * @param lineSpacing          line-height multiplier for body paragraphs
 * @param sectionSpacingBefore space above each section heading
 * @param ruleWidth            stroke width of section divider lines
 * @author DevLaunch
 */
public record PdfTemplateStyle(
        Color headingColor,
        Color textColor,
        Color mutedColor,
        Color ruleColor,
        Color bandTextColor,
        Color bandMutedColor,
        Color highlightFill,
        HeaderLayout headerLayout,
        boolean contactColumns,
        boolean uppercaseName,
        boolean headingBar,
        boolean coloredEntryTitles,
        boolean skillsHighlight,
        float nameSize,
        float sectionSize,
        float bodySize,
        float metaSize,
        float margin,
        float lineSpacing,
        float sectionSpacingBefore,
        float ruleWidth) {

    /**
     * How the candidate header (name, headline, contact details) is
     * arranged on the first page.
     */
    public enum HeaderLayout {
        /** Centred name, headline, and contact details. */
        CENTERED,
        /** Left-aligned header for a relaxed, airy feel. */
        LEFT_ALIGNED,
        /** A full-width coloured band behind the header content. */
        BAND
    }

}
