package com.devlaunch.service;

import com.devlaunch.service.PdfTemplateStyle.HeaderLayout;

import java.awt.Color;

/**
 * The professionally designed PDF layout templates available for
 * downloading a resume.
 * <p>
 * Each template changes presentation only — colours, typography,
 * spacing, and the layout-level choices in {@link PdfTemplateStyle}.
 * The resume content rendered by {@code ResumePdfServiceImpl} is
 * identical for every template so the same sections remain ATS-friendly
 * regardless of the chosen style.
 * </p>
 * <p>
 * The layout value is passed to the download endpoint as a query
 * parameter, e.g. {@code GET /api/resumes/{id}/pdf?template=MODERN_BLUE}.
 * </p>
 *
 * @author DevLaunch
 */
public enum ResumePdfTemplate {

    /**
     * A traditional corporate resume: centred header, single column,
     * clean black headings, and standard spacing. The safest choice
     * for ATS screening.
     */
    CLASSIC_PROFESSIONAL(new PdfTemplateStyle(
            new Color(0x11, 0x18, 0x27),
            new Color(0x11, 0x18, 0x27),
            new Color(0x4B, 0x55, 0x63),
            new Color(0xD1, 0xD5, 0xDB),
            Color.WHITE,
            Color.LIGHT_GRAY,
            null,
            HeaderLayout.CENTERED,
            false,
            false,
            false,
            false,
            false,
            22f, 12f, 10f, 9.5f,
            48f, 1.35f, 18f, 0.7f)),

    /**
     * A modern layout with a two-column contact grid, blue accent bars
     * above the headings, blue entry titles, and elegant divider lines.
     */
    MODERN_BLUE(new PdfTemplateStyle(
            new Color(0x1D, 0x4E, 0xD8),
            new Color(0x11, 0x18, 0x27),
            new Color(0x4B, 0x55, 0x63),
            new Color(0x93, 0xC5, 0xFD),
            Color.WHITE,
            Color.LIGHT_GRAY,
            null,
            HeaderLayout.CENTERED,
            true,
            false,
            true,
            true,
            false,
            24f, 12.5f, 10f, 9.5f,
            50f, 1.4f, 18f, 0.9f)),

    /**
     * An elegant, premium-minimal resume: left-aligned header, smaller
     * headings, thin separators, and generous white space.
     */
    MINIMAL(new PdfTemplateStyle(
            new Color(0x37, 0x41, 0x51),
            new Color(0x1F, 0x29, 0x37),
            new Color(0x6B, 0x72, 0x80),
            new Color(0xE5, 0xE7, 0xEB),
            Color.WHITE,
            Color.LIGHT_GRAY,
            null,
            HeaderLayout.LEFT_ALIGNED,
            false,
            false,
            false,
            false,
            false,
            20f, 10.5f, 10f, 9f,
            56f, 1.6f, 22f, 0.5f)),

    /**
     * A premium resume for senior professionals: large uppercase name,
     * strong typography hierarchy, heavier rules, and wider margins.
     */
    EXECUTIVE(new PdfTemplateStyle(
            new Color(0x0F, 0x17, 0x2A),
            new Color(0x0F, 0x17, 0x2A),
            new Color(0x47, 0x55, 0x69),
            new Color(0x94, 0xA3, 0xB8),
            Color.WHITE,
            Color.LIGHT_GRAY,
            null,
            HeaderLayout.CENTERED,
            false,
            true,
            false,
            false,
            false,
            28f, 12.5f, 10.5f, 10f,
            60f, 1.4f, 20f, 1.0f)),

    /**
     * A colourful, visually attractive resume with a coloured header
     * band, accent-bar headings, and a highlighted skills box. Still
     * text-first and printable.
     */
    CREATIVE(new PdfTemplateStyle(
            new Color(0x4F, 0x46, 0xE5),
            new Color(0x11, 0x18, 0x27),
            new Color(0x6B, 0x72, 0x80),
            new Color(0xC7, 0xD2, 0xFE),
            Color.WHITE,
            new Color(0xE0, 0xE7, 0xFF),
            new Color(0xEE, 0xF2, 0xFF),
            HeaderLayout.BAND,
            false,
            false,
            true,
            false,
            true,
            22f, 12f, 10f, 9.5f,
            48f, 1.4f, 18f, 0.7f));

    private final PdfTemplateStyle style;

    ResumePdfTemplate(final PdfTemplateStyle style) {
        this.style = style;
    }

    /**
     * Returns the visual configuration for this template.
     *
     * @return the template's {@link PdfTemplateStyle}
     */
    public PdfTemplateStyle getStyle() {
        return style;
    }

    /**
     * Resolves a template from its name, case-insensitively.
     * <p>
     * Used by the controller to convert the {@code ?template=} query
     * parameter, rejecting unknown values with an
     * {@link IllegalArgumentException} so clients receive a 400 rather
     * than a generic server error.
     * </p>
     *
     * @param value the template name, e.g. {@code MODERN_BLUE}
     * @return the matching {@link ResumePdfTemplate}
     * @throws IllegalArgumentException if the value matches no template
     */
    public static ResumePdfTemplate from(final String value) {
        for (final ResumePdfTemplate template : values()) {
            if (template.name().equalsIgnoreCase(value)) {
                return template;
            }
        }
        throw new IllegalArgumentException("Unknown resume PDF template: " + value);
    }

}
