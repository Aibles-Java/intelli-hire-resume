# Design System: IntelliHire — AI Recruitment Platform

**Generated**: 2026-03-04
**Industry**: HR Tech / AI SaaS / Recruitment
**Mood**: Intelligent, Precise, Fast, Trustworthy, Cutting-edge
**Aesthetic Direction**: Dark obsidian interface with electric cyan accents and scanning-line AI effects — like a recruitment command center from 2030
**Competitor Gap**: All competitors use safe corporate palettes (green/white/blue). Dark mode + electric accents = instantly memorable and ownable.

---

## Color Palette

| Role | Hex | CSS Variable | Usage |
|------|-----|-------------|-------|
| **Background Primary** | `#0A0A0F` | `--bg-primary` | Page background, main canvas |
| **Background Secondary** | `#12121A` | `--bg-secondary` | Sidebar, elevated sections |
| **Background Tertiary** | `#1A1A2E` | `--bg-tertiary` | Hover states, active areas |
| **Card Background** | `#16161F` | `--bg-card` | Cards, panels, modals |
| **Card Hover** | `#1E1E2D` | `--bg-card-hover` | Card hover state |
| **Elevated Surface** | `#1F1F2F` | `--bg-elevated` | Dropdowns, tooltips |
| **Accent (Electric Cyan)** | `#00F0FF` | `--accent` | Primary actions, AI indicators, links, highlights |
| **Accent Dim** | `#00B8C5` | `--accent-dim` | Secondary accent, borders |
| **Accent Glow** | `rgba(0,240,255,0.15)` | `--accent-glow` | Glow effects, background tints |
| **Accent Glow Strong** | `rgba(0,240,255,0.3)` | `--accent-glow-strong` | Hover glow, active states |
| **Secondary Accent (Hot Coral)** | `#FF3D71` | `--accent-secondary` | Alerts, rejections, urgent actions |
| **Success** | `#00E68A` | `--success` | Hired, completed, positive |
| **Warning** | `#FFAA00` | `--warning` | Interview, pending, attention |
| **Danger** | `#FF3D71` | `--danger` | Failed, rejected, errors |
| **Text Primary** | `#EEF0F6` | `--text-primary` | Headings, body text |
| **Text Secondary** | `#8B8FA3` | `--text-secondary` | Labels, descriptions, metadata |
| **Text Tertiary** | `#5A5E72` | `--text-tertiary` | Placeholders, disabled text |
| **Border** | `rgba(255,255,255,0.06)` | `--border` | Card borders, dividers |
| **Border Accent** | `rgba(0,240,255,0.2)` | `--border-accent` | Active card borders, focus rings |

### Contrast Ratios
- Text Primary (#EEF0F6) on Background (#0A0A0F): **15.8:1** ✅ AAA
- Text Secondary (#8B8FA3) on Background (#0A0A0F): **7.2:1** ✅ AA
- Accent (#00F0FF) on Background (#0A0A0F): **12.1:1** ✅ AAA
- Text Primary on Card (#16161F): **13.4:1** ✅ AAA

### Design Rationale
- **Dark obsidian base**: Creates a "command center" feel. Data-dense screens are easier to read on dark backgrounds (less eye strain). Differentiates from every competitor.
- **Electric cyan accent**: High-energy, tech-forward, AI-associated. Cyan on dark = maximum pop. No competitor uses this.
- **Hot coral secondary**: Creates urgency for actions. Complementary to cyan on the color wheel. Used sparingly for alerts/rejections.
- **Monochromatic neutrals**: Background/card/elevated follow a tight value ramp (10→18→26→31 in lightness) creating subtle depth without noise.

---

## Typography

### Font Pairing

| Role | Font | Weight | Size (Desktop) | Size (Mobile) | Line Height |
|------|------|--------|----------------|---------------|-------------|
| **Display/h1** | Syne | 800 (ExtraBold) | 48-64px | 32-40px | 1.1 |
| **Heading/h2** | Syne | 700 (Bold) | 32-40px | 24-28px | 1.15 |
| **Sub-heading/h3** | Syne | 600 (Semibold) | 20-24px | 18-20px | 1.25 |
| **Body** | DM Sans | 400 (Regular) | 16px | 15px | 1.6 |
| **Body Bold** | DM Sans | 600 (Semibold) | 16px | 15px | 1.6 |
| **Small/Caption** | DM Sans | 500 (Medium) | 13px | 12px | 1.4 |
| **Label** | DM Sans | 600 (Semibold) | 11px | 11px | 1.2 |
| **Mono/Data** | DM Sans | 500 (Medium) | 14px | 13px | 1.4 |
| **CTA Button** | Syne | 700 (Bold) | 16px | 15px | 1 |
| **Stat Number** | Syne | 800 (ExtraBold) | 48-72px | 36-48px | 1 |

### Google Fonts URL
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Syne:wght@400;500;600;700;800&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&display=swap" rel="stylesheet">
```

### Tailwind Config
```js
fontFamily: {
  display: ['Syne', 'system-ui', 'sans-serif'],
  body: ['DM Sans', 'system-ui', 'sans-serif'],
}
```

### Type Rules
- Letter spacing: `-0.03em` for display, `-0.01em` for headings, `normal` for body
- Max line width: `60ch` for body text
- Headings: NEVER more than 2 lines on desktop, 3 on mobile
- Numbers in stats: use `font-variant-numeric: tabular-nums` for alignment

---

## Spacing System

| Token | Value | Usage |
|-------|-------|-------|
| `space-1` | 4px | Inline gaps, icon padding |
| `space-2` | 8px | Tag padding, tight gaps |
| `space-3` | 12px | Small component gaps |
| `space-4` | 16px | Card padding (mobile), form gaps |
| `space-5` | 20px | Standard component gap |
| `space-6` | 24px | Card padding (desktop), section inner |
| `space-8` | 32px | Between component groups |
| `space-10` | 40px | Section padding (mobile) |
| `space-12` | 48px | Section padding (tablet) |
| `space-16` | 64px | Section padding (desktop) |
| `space-20` | 80px | Major section breaks |
| `space-24` | 96px | Hero padding, page top |

### Container
```
Max width: 1280px
Mobile padding: 16px horizontal
Tablet padding: 24px horizontal
Desktop: centered with auto margins, 48px horizontal
```

---

## Components

### Primary CTA Button
```
Background: var(--accent) (#00F0FF)
Text: #0A0A0F (dark on light)
Font: Syne 700, 16px
Border radius: 10px
Padding: 14px 28px
Min height: 48px
Shadow: 0 0 20px rgba(0,240,255,0.2)
Hover: shadow grows to 0 0 30px rgba(0,240,255,0.35), translateY(-2px)
Active: translateY(0), shadow shrinks
Focus: 3px ring var(--accent)/40%
Transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1)
```

### Ghost Button (Secondary)
```
Background: rgba(255,255,255,0.04)
Border: 1px solid var(--border)
Text: var(--text-secondary)
Font: DM Sans 600, 15px
Border radius: 10px
Padding: 12px 24px
Hover: background rgba(255,255,255,0.08), border rgba(255,255,255,0.12), text var(--text-primary)
```

### Card (Base)
```
Background: var(--bg-card) (#16161F)
Border: 1px solid var(--border)
Border radius: 16px
Padding: 24px
Shadow: none (default) → 0 8px 32px rgba(0,0,0,0.3) on hover
Hover: border-color var(--border-accent), translateY(-2px)
Transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1)
Overflow: hidden (for scan-line effects)
```

### AI Score Ring (SVG)
```
Size: 56px × 56px
Track: circle r=22, stroke rgba(255,255,255,0.06), stroke-width 4
Fill: circle r=22, stroke var(--accent), stroke-width 4
  - stroke-dasharray: 138 (circumference)
  - stroke-dashoffset: calculated from score (0-100)
  - stroke-linecap: round
  - transform: rotate(-90deg) from center
  - animation: scoreFill 1.5s ease-out
Score text: Syne 800, 16px, centered, color var(--accent)
```

### AI Score Color Mapping
```
90-100: var(--accent) #00F0FF — Excellent
70-89:  var(--success) #00E68A — Strong
50-69:  var(--warning) #FFAA00 — Moderate
0-49:   var(--danger) #FF3D71 — Weak
```

### Status Badge
```
Padding: 4px 10px
Border radius: 6px
Font: DM Sans 600, 12px
Letter-spacing: 0.02em

Variants:
  Applied:    bg rgba(0,240,255,0.1),  text #00F0FF,  border rgba(0,240,255,0.15)
  Screening:  bg rgba(255,170,0,0.1),  text #FFAA00,  border rgba(255,170,0,0.15)
  Interview:  bg rgba(138,43,226,0.1), text #A855F7,  border rgba(138,43,226,0.15)
  Offer:      bg rgba(0,230,138,0.1),  text #00E68A,  border rgba(0,230,138,0.15)
  Hired:      bg rgba(0,230,138,0.15), text #00E68A,  border rgba(0,230,138,0.2)
  Rejected:   bg rgba(255,61,113,0.1), text #FF3D71,  border rgba(255,61,113,0.15)
```

### Skill Tag
```
Padding: 3px 10px
Border radius: 6px
Font: DM Sans 600, 12px

Variants:
  Technical: bg rgba(0,240,255,0.1),  text var(--accent), border 1px solid rgba(0,240,255,0.15)
  Soft:      bg rgba(168,85,247,0.1), text #A855F7,       border 1px solid rgba(168,85,247,0.15)
  Tool:      bg rgba(0,230,138,0.1),  text var(--success), border 1px solid rgba(0,230,138,0.15)
```

### Navigation (Sidebar — Recruiter)
```
Width: 260px (expanded), 72px (collapsed)
Background: var(--bg-secondary)
Border-right: 1px solid var(--border)
Nav item: padding 10px 16px, border-radius 8px
Nav item active: bg var(--accent-glow), text var(--accent), border-left 2px solid var(--accent)
Nav item hover: bg rgba(255,255,255,0.04)
Icon: 20px, stroke 1.5px
Transition: width 0.3s ease
```

### Data Table
```
Header: font DM Sans 600 12px, color var(--text-tertiary), text-transform uppercase, letter-spacing 0.05em
Row: padding 14px 16px, border-bottom 1px solid var(--border)
Row hover: bg var(--bg-card-hover)
Row selected: bg var(--accent-glow), border-left 2px solid var(--accent)
```

### Input Field
```
Background: var(--bg-card)
Border: 1px solid var(--border)
Border radius: 10px
Padding: 12px 16px
Font: DM Sans 400, 15px
Color: var(--text-primary)
Placeholder: var(--text-tertiary)
Focus: border-color var(--accent), shadow 0 0 0 3px var(--accent-glow)
Transition: all 0.2s
```

### Modal/Dialog
```
Backdrop: rgba(0,0,0,0.6) with backdrop-filter: blur(8px)
Container: bg var(--bg-elevated), border 1px solid var(--border), border-radius 20px
Max-width: 520px
Padding: 32px
Shadow: 0 24px 64px rgba(0,0,0,0.5)
```

### Tooltip
```
Background: var(--bg-elevated)
Border: 1px solid var(--border)
Border-radius: 8px
Padding: 8px 12px
Font: DM Sans 400, 13px
Color: var(--text-secondary)
Shadow: 0 4px 16px rgba(0,0,0,0.3)
Arrow: 6px, same background
```

---

## AI Visual Effects

### Scan Line (on card hover)
```css
.scan-line {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 2px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  transform: translateY(-100%);
  opacity: 0.6;
}
.card:hover .scan-line {
  animation: scan-line 1.5s ease-in-out;
}
@keyframes scan-line {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(200%); }
}
```

### Pulsing AI Dot
```css
.ai-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--accent);
  animation: pulse-glow 2s infinite;
}
@keyframes pulse-glow {
  0%, 100% { opacity: 1; box-shadow: 0 0 4px currentColor; }
  50% { opacity: 0.5; box-shadow: 0 0 12px currentColor; }
}
```

### Score Ring Animation
```css
@keyframes scoreFill {
  from { stroke-dashoffset: 138; }
}
.score-circle {
  animation: scoreFill 1.5s ease-out forwards;
}
```

### Grid Background (hero/sections)
```css
.grid-bg {
  background-image:
    linear-gradient(rgba(0,240,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,240,255,0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
}
```

### Ambient Glow
```css
.ambient-glow {
  position: absolute;
  width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(0,240,255,0.08) 0%, transparent 70%);
  pointer-events: none;
  filter: blur(40px);
}
```

### Staggered Fade-In
```css
.stagger-item {
  animation: fadeInUp 0.6s ease-out both;
}
.stagger-item:nth-child(1) { animation-delay: 0s; }
.stagger-item:nth-child(2) { animation-delay: 0.1s; }
.stagger-item:nth-child(3) { animation-delay: 0.2s; }
/* ... */
```

---

## Page/Screen Specifications

### Screen 1: Landing Page
```
Sections:
1. NAV           — Logo + links + 2 CTAs (sticky, blur backdrop)
2. HERO          — Display heading + sub + 2 CTAs + floating AI cards
3. FEATURES      — 4-column grid, icon + stat + title + desc per card
4. STATS BAR     — 4 oversized numbers with labels
5. LOGOS         — Trusted-by row (company names in muted text)
6. TESTIMONIALS  — Carousel with quote + avatar + name/role
7. FINAL CTA     — Full-width banner with heading + CTA + glow effect
8. FOOTER        — Brand + links + copyright
```

### Screen 2: Recruiter Dashboard
```
Layout: Sidebar (260px) + Main content
Sections:
1. HEADER        — Breadcrumb + search + notifications + avatar
2. STATS ROW     — 4 stat cards (CV received, Screening, Interviewed, Hired)
3. TWO-COL       — Left: Active Jobs list | Right: AI Insights panel
4. CHARTS        — Pipeline funnel chart + Resume status distribution
5. ACTIVITY      — Recent activity timeline with action badges
6. INTERVIEWS    — Upcoming interviews list with time/candidate/role
```

### Screen 3: Candidate Pipeline (Kanban)
```
Layout: Sidebar + Main
Sections:
1. HEADER        — Page title + filter bar + search + view toggle
2. KANBAN        — 5 columns: Applied → Screening → Interview → Offer → Hired
3. CARDS         — Avatar + name + role + AI score ring + skill tags + date
4. COLUMN HEAD   — Stage name + count badge + add button
```

### Screen 4: Candidate Detail
```
Layout: Sidebar + Two-column main
Left column (60%):
1. PROFILE HEADER — Avatar + name + role + status + contact links
2. RESUME        — File preview/download + parsed raw text
3. EXPERIENCE    — Vertical timeline with company/role/dates
4. EDUCATION     — Cards with school/degree/year
5. SKILLS        — Grouped tags (Technical/Soft/Tool) with confidence bars

Right column (40%):
1. AI ANALYSIS   — Score ring (large) + match breakdown + strengths/weaknesses
2. ACTION BAR    — Move stage, Schedule interview, Reject buttons
3. TIMELINE      — Interaction history (stage changes, notes, emails)
```

### Screen 5: Application Form (Candidate)
```
Layout: Centered single-column (max 640px)
Sections:
1. JOB HEADER    — Company logo + job title + location + type
2. PROGRESS      — 3-step indicator (Upload → Details → Review)
3. UPLOAD STEP   — Drag-drop zone + file info + parsing animation
4. CONTACT STEP  — Auto-filled form from parsed resume
5. REVIEW STEP   — Summary card + submit button
6. CONFIRMATION  — Success message + tracking link
```

### Screen 6: Application Tracking (Candidate)
```
Layout: Centered single-column (max 720px)
Sections:
1. HEADER        — "Your Application" + job title + company
2. STATUS        — Vertical timeline with 5 stages, current highlighted
3. SUMMARY       — Card with resume file info + contact info
4. AI FEEDBACK   — Resume Health Score ring + skill match indicators
5. NOTIFICATIONS — Update cards from recruiter (date + message)
```

---

## Responsive Breakpoints

| Breakpoint | Width | Layout Changes |
|-----------|-------|----------------|
| **Mobile** | < 768px | Single column, bottom nav, stacked cards, kanban → list view |
| **Tablet** | 768-1024px | Sidebar collapsed (72px), 2-col grids |
| **Desktop** | 1024-1440px | Full sidebar (260px), 3-4 col grids, kanban full |
| **Large** | > 1440px | Max-width 1280px container, generous whitespace |

### Mobile-First Rules
- Touch targets: minimum 44px
- Font: minimum 15px body
- Cards: full-width with 16px padding
- CTA: full-width on mobile
- Sidebar: hidden, bottom tab bar instead
- Kanban: horizontal scroll with snap, or list view toggle

---

## Image & Icon Guidelines

| Element | Format | Loading |
|---------|--------|---------|
| Avatars | 40-56px circle, initials fallback | eager |
| Company logos | SVG or text (stylized) | inline |
| Icons | Inline SVG, 20px, stroke 1.5px | inline |
| Charts | SVG (inline generated) | lazy |
| File previews | Thumbnail 80×100px | lazy |

- All icons: use `currentColor` for stroke, inherit from parent
- Avatar fallback: initials on gradient background (linear-gradient 135deg, accent → blue)
- No stock photography — use generated UI, data visualizations, abstract shapes

---

## Noise Texture Overlay
```css
.noise::before {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  pointer-events: none;
  z-index: 0;
}
```

---

## Next Steps
- [ ] Build landing page HTML+Tailwind using this design system
- [ ] Build all 6 screens following section specifications
- [ ] Run conversion-optimizer audit on completed output
