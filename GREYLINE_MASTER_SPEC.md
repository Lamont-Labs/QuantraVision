# GREYLINE-OS: MASTER SPECIFICATION
**Version:** 3.3  
**Date:** November 21, 2025  
**Status:** Ready for Development

---

## PROJECT VISION

**Greyline-OS** is a self-contained, offline-first desktop application that transforms the KDP publishing process into a one-click operation. It bundles local AI models to generate complete, publication-ready novels including manuscript, cover art, blurb, and metadata—entirely free and offline after initial setup.

**Core Philosophy:**
- **100% Free:** No API costs, no subscriptions, no cloud dependencies
- **Deterministic:** Rule-locked generation prevents AI drift
- **Self-Contained:** Everything bundled in one application (~46GB)
- **Personal Use:** Designed for NucBox K6 desktop (32GB RAM, 1TB SSD)
- **Complete Automation:** Double-click icon → wait 30-60 min → KDP-ready novel package

**Target Workflow:**
```
Double-click Greyline icon
  ↓
Conversational intake wizard (15-30 min)
  ↓
Visual world builder (30-60 min)
  ↓
Click "Generate Novel"
  ↓
Wait 30-60 minutes (AI writes entire book)
  ↓
Review/edit (optional)
  ↓
Click "Export to KDP"
  ↓
Upload to Amazon and publish
```

**Tagline:** "Deterministic Publishing Engine"

---

## VISUAL IDENTITY

### Logo Concept
- Open book with circuit tree growing from pages
- Electric cyan/teal glowing branches
- Deep navy blue background
- Clean, modern typography
- **Reference:** Lamont Labs aesthetic (blue circuit gear design)

### Color Palette
- **Primary Background:** Deep navy blue (#0a1929)
- **Accent:** Electric cyan (#00e5ff)
- **Secondary:** Teal (#00acc1)
- **Text:** White (#ffffff) and light blue (#b3e5fc)
- **Highlights:** Glowing cyan with subtle pulse effects

### Design Language
- Dark blue windows with subtle texture
- Circuit pattern motifs in backgrounds
- Glowing cyan outlines on interactive elements
- Card-based layouts with soft blue glows
- Clean sans-serif typography (similar to "LAMONT LABS")
- Smooth animations with light trail effects
- Progress bars that pulse with energy
- Spinning gear animations during generation

### UI Components
- **Intake Wizard:** Conversational bubbles with blue gradients, one question at a time
- **World Builder:** Character/location cards with glowing borders, circuit-trace connection lines
- **Generation Screen:** Animated gear icon, chapter progress with cyan fill
- **Export Panel:** Clean checklist with glowing checkmarks

---

## CORE ARCHITECTURE

### Technology Stack
- **Framework:** Electron (cross-platform desktop app)
- **Frontend:** React with TailwindCSS
- **Backend:** Node.js
- **AI Integration:** node-llama-cpp for model inference
- **Database:** SQLite (encrypted local storage)
- **Export:** docx, pdf-lib, markdown-it libraries

### Folder Structure
```
greyline-os/
├── greyline.exe (Windows) / greyline.app (Mac)
├── models/
│   ├── mixtral-8x7b.gguf (26GB)
│   ├── qwen-14b.gguf (8GB)
│   ├── llama-8b.gguf (4.7GB)
│   └── sdxl/ (7GB)
├── resources/
│   ├── icons/
│   ├── templates/
│   └── assets/
└── projects/
    └── [user-generated projects]
```

### Total Size: ~46GB

---

## AI MODEL ARCHITECTURE

All models must be **open-licensed** for free commercial use.

### Model Roles

**1. Mixtral 8x7B** (Apache 2.0) - ~26GB
- **Purpose:** Creative prose generation
- **Use Cases:**
  - Chapter writing
  - Dialogue creation
  - Vivid descriptions
  - Sensory/emotion enhancement passes

**2. Qwen 14B** (Apache 2.0) - ~8GB
- **Purpose:** Reasoning, planning, structure
- **Use Cases:**
  - Story outlines
  - Plot architecture
  - Character arc planning
  - Story structure validation

**3. Llama 3.1 8B** (Meta license, commercial OK) - ~4.7GB
- **Purpose:** Fast edits and validation
- **Use Cases:**
  - Quick consistency checks
  - Blurb generation
  - Keyword extraction
  - Surgical fixes

**4. SDXL** (OpenRAIL) - ~7GB
- **Purpose:** Cover art generation
- **Use Cases:**
  - Book cover design (4 options per generation)
  - Genre-appropriate imagery

### Model Loading Strategy
- Load models on-demand (not all at once)
- Unload inactive models to free RAM
- User sees loading progress for each model
- Mixtral loaded by default (most frequently used)

---

## COMPLETE FEATURES LIST

### ✅ CORE FEATURES (INCLUDED)

#### 1. Universal Intake Wizard
- Conversational one-question-at-a-time interface
- Adapts dynamically based on project type
- **Supported Types:**
  - Fiction (novels, short stories)
  - Nonfiction (how-to, self-help, business)
  - Memoir (personal stories, autobiography)
  - Journal (gratitude, trading, mental health, academic)
  - Poetry
  - Screenplay
  - RPG Sourcebooks
  - Technical Manuals
  - Hybrid projects (any combination)
- Fail-closed validation (catches errors immediately)
- Auto-completion from uploaded notes/text
- Creates immutable "Lock Sheet" to prevent AI drift

#### 2. Series Management
- Standalone or multi-book projects
- **Structures:** Duology, Trilogy, Saga (4-9 books), Shared Universe
- Cross-book continuity tracking
- Character/world consistency enforcement across volumes
- Timeline synchronization between books
- Automatic arc mapping for series-wide story structure

#### 3. Advanced Genre System
- 20+ primary genres
- 100+ subgenre combinations
- Custom genre blending
- Genre-specific pacing, tone, and structure rules
- Archetypal story structure templates per genre

#### 4. Voice/POV/Tense Lock
- Ironclad narrative consistency enforcement
- **Supported:**
  - 1st person / 3rd person limited / 3rd person omniscient / Multiple POV
  - Present tense / Past tense / Mixed
- Real-time drift detection and auto-correction
- Validation on every generated section

#### 5. Industrial-Strength World Builder (Fiction/RPG)
- **Components:**
  - Cosmology & creation myths
  - Geography with location cards
  - Factions, cultures, politics
  - Magic/tech systems (rules, costs, limits, mechanics)
  - Historical timeline builder
  - Religion, mythology, taboos
  - Government & social structures
  - Economy, commerce, currency
  - Arts, cuisine, architecture, transportation
  - Key artifacts and relics
- Generates searchable **World Bible**
- Referenced by AI during every chapter generation

#### 6. Character Ledger System (Fiction/Memoir)
- **Deep psychological profiles:**
  - Physical descriptions
  - Personality traits, fears, desires
  - Strengths, flaws, contradictions
  - Character arc mapping (start → transformation → end)
  - Relationship graphs between characters
  - Voice notes (speech patterns, dialect, cadence)
  - Motivations and internal conflicts
- Prevents character drift throughout manuscript

#### 7. Nonfiction Suite
- Thesis statement capture
- Argument structure builder (3-10 core arguments)
- Case study library
- Anecdote management
- Citation tracking (optional)
- Counterargument mapping
- Reader transformation outcome definition
- Logical flow enforcement

#### 8. Memoir Suite
- Vignette list builder (key life moments)
- Emotional arc tracking
- Identity anchor definition
- Internal vs external conflict mapping
- Personal timeline constructor
- Vulnerability depth controls
- Reflection density settings

#### 9. Journal Suite
- **Modes:** Gratitude, Trading, Mental Health, Affirmations, Academic
- **Duration Templates:** 30/60/90/365 days
- Custom daily prompts
- Reflection depth controls
- Habit tracking and streak metrics
- Printable KDP-ready journal pages
- Optional inspirational quotes

#### 10. Intelligent Wordcount Engine
- Target-based or organic growth modes
- Auto-calculates optimal chapter count
- Determines sections per chapter
- Assigns 500-800 word segments for consistency
- Enforces no-drift length rules
- Automatic splitting/merging of sections
- Prevents token overruns

#### 11. Three Generation Modes
1. **Section-by-Section:** Generate one segment at a time with manual review
2. **Chapter-by-Chapter:** Generate full chapters with checkpoints (recommended)
3. **Full Auto:** One-click complete manuscript generation (30-60 min)

#### 12. Chapter-by-Chapter Generation with Validation
**Flow:**
1. Generate Chapter 1 (2,500 words)
2. Validation Pass (30 seconds):
   - POV consistency
   - Tense accuracy
   - Voice matching
   - World rule violations
   - Character behavior
3. If errors → Auto-regenerate
4. If clean → Lock Chapter 1 as canon
5. Generate Chapter 2 (references Chapter 1 summary + outline)
6. Cross-chapter consistency check
7. Repeat for all chapters

**Benefits:**
- No context window overload
- Drift caught early
- Progressive context building
- Recoverable failures
- Better quality output

#### 13. Two-Stage Generation (Optimization)
**Stage 1: Rapid Prototype (15-20 min)**
- Generate full outline + first 3 chapters
- User reviews direction, voice, quality
- Decision point: Continue or regenerate with adjustments

**Stage 2: Full Production (30-40 min)**
- Generate remaining chapters
- Polish passes
- Export preparation

#### 14. Drift Guard System
- Real-time validation during generation
- Checks POV, tense, voice, character consistency
- World rule compliance monitoring
- Automatic regeneration on drift detection
- Drift logging and pattern analysis

#### 15. Theme & Motif Engine
- Captures 3-5 thematic pillars
- Enforces motif recurrence throughout manuscript
- Emotional arc alignment
- Structural resonance checkpoints
- Ensures thematic consistency from start to finish

#### 16. AI Cover Generator
- Genre-aware cover design
- 4 design options per generation (2 min)
- Customizable: mood, elements, color palette, composition
- Text placement editor (title, author, tagline)
- Exports at print resolution (6×9, 300 DPI)
- KDP-ready PNG/PDF formats

#### 17. Smart Manuscript Editor
- Split-screen interface
- AI suggestions sidebar:
  - Weak verb identification
  - Pacing issues
  - Voice drift warnings
  - Character inconsistencies
- Accept/Reject/Rewrite options
- Section-level editing
- Search and replace
- Word count tracking

#### 18. Blurb & Metadata Generator
- AI-generated book descriptions (200-300 words)
- Hook-focused, conversion-optimized copy
- Keyword research (7 KDP keywords auto-generated)
- Category recommendations
- Target audience analysis
- Comp title suggestions

#### 19. Multi-Format Export Pipeline
**Complete KDP Package Generated:**

**For Fiction/Nonfiction/Memoir:**
```
[Project Name]-KDP-Package/
├── Print/
│   ├── Manuscript.docx (print-ready formatting)
│   ├── Manuscript.pdf (proof copy)
│   └── Cover-Print.pdf (6×9 with bleed)
├── eBook/
│   ├── Manuscript.epub (Kindle upload)
│   ├── Manuscript.docx (alternative format)
│   └── Cover-eBook.jpg (1600×2560 px)
└── Marketing/
    ├── Blurb.txt
    ├── Keywords.txt (7 KDP keywords)
    └── Metadata.txt (categories, audience, etc.)
```

**For Journals:**
```
[Journal Name]-KDP-Package/
├── Interior-Pages.pdf (formatted journal pages)
├── Cover.pdf (with spine calculated from page count)
└── Preview.pdf (sample pages for listing)
```

**Supported Export Formats:**
- DOCX (KDP manuscript upload)
- PDF (print-ready with bleed, margins, trim size)
- EPUB (eBook format)
- Markdown (plain text editing)

#### 20. KDP-Ready Formatting
- Automatic page size configuration (6×9, 5×8, custom)
- Professional fonts and spacing
- Title page generation
- Copyright page (auto-populated)
- Table of contents (auto-generated from chapters)
- Chapter headers with consistent styling
- Page numbering
- Front/back matter templates

#### 21. Project Management
- Multiple simultaneous projects
- Project templates (save intake settings for reuse)
- Version history and snapshots
- Project cloning (for series books)
- Search across all projects
- Tags and organization
- Auto-save every chapter
- Crash recovery

#### 22. Expansion & Polish Passes
- **Sensory Pass:** Adds vivid sensory details
- **Emotion Pass:** Deepens emotional impact
- **Dialogue Pass:** Sharpens conversation realism
- **Pacing Pass:** Adjusts chapter rhythm
- **Show-Don't-Tell Pass:** Converts exposition to scenes
- Configurable (turn passes on/off per project)

#### 23. Validation & Quality Control
- POV consistency checker
- Tense drift detector
- Character voice analyzer
- World rule violation scanner
- Plot hole identifier
- Pacing analysis
- Readability scoring (Flesch-Kincaid, Gunning Fog)

#### 24. Offline-First Architecture
- Zero internet required after setup
- All AI processing on-device
- No cloud dependencies
- No subscriptions
- Complete data privacy
- All data stored locally in encrypted SQLite database

#### 25. Quick Start vs Deep Dive Modes
**Quick Start Mode (10-15 min):**
- Project type, genre, POV, wordcount
- 3 character profiles (basic)
- Premise (1-2 sentences)
- Click generate

**Deep Dive Mode (60+ min):**
- Full worldbuilding suite
- Character psychology
- Theme mapping
- Complete intake wizard

User chooses their path based on need.

#### 26. Template Library
Pre-built common setups:
- "YA Dystopian Trilogy Template"
- "Cozy Mystery Series Template"
- "Epic Fantasy Standalone Template"
- "Romance Novella Template"
- "Trading Journal Template"
- "Gratitude Journal Template"

Users can start from templates and customize.

#### 27. Generation Speed Tiers
- **Fast Draft (15-20 min):** Single pass, lighter validation
- **Balanced (30-40 min):** Multiple passes, standard validation (recommended)
- **Premium (60+ min):** All enhancement passes, deep validation

---

### ❌ FEATURES CUT (Not Included in v1)

- Historical learning (complex, minimal benefit)
- Collaborative mode (designed for single user)
- Translation engine (massive scope creep)
- Batch operations (overkill for personal use)
- Publishing analytics (manual tracking sufficient)
- AI Narrator/audiobook (future stretch goal)
- Comic book mode (too niche)
- Marketing copy generator (nice-to-have, not essential)

---

## INTAKE WIZARD SPECIFICATION

### Design Principles
1. **One question per turn** → minimizes ambiguity
2. **Fail-closed** → invalid answers produce correction loop
3. **Adaptive branching** → only relevant questions shown
4. **Auto-completion** → extract fields from uploaded text
5. **Organic scaling** → AI estimates structure
6. **Universal compatibility** → works for all project types

### Primary Fields Captured

#### Base Questions (All Projects)
1. Project Type (fiction, nonfiction, memoir, journal, etc.)
2. Series Structure (standalone, duology, trilogy, saga, shared universe)
3. Title (optional, AI can generate)
4. Target Wordcount or "Organic"

#### Fiction-Specific
5. Genre + Subgenre
6. Audience Level (Children, MG, YA, Adult, Professional/Academic)
7. POV (1st person, 3rd limited, 3rd omniscient, multiple)
8. Tense (present, past, mixed)
9. Voice Description (2-3 sentences)
10. Core Themes (3-5)
11. Core Message/Emotional Takeaway
12. Worldbuilding Suite (see section above)
13. Character Ledger (see section above)

#### Nonfiction-Specific
5. Thesis Statement (1 sentence)
6. Arguments (3-10 core points)
7. Case Studies/Examples
8. Anecdotes
9. Counterarguments
10. Reader Transformation Goal

#### Memoir-Specific
5. Vignettes (key life moments)
6. Emotional Arcs
7. Identity Anchors
8. Timeline
9. Vulnerability Depth

#### Journal-Specific
5. Mode (gratitude, trading, mental health, etc.)
6. Duration (30/60/90/365 days)
7. Prompts per Day
8. Reflection Depth

### Lock Sheet Output
The wizard produces an immutable configuration containing:
- All metadata
- Voice/POV/Tense locks
- Wordcount targets
- Chapter/section scaffold
- Character ledger
- Theme/motif ledger
- World Bible
- Timeline stack
- Export preferences
- Drift guard parameters

This Lock Sheet is the blueprint for ALL downstream generation.

---

## GENERATION ENGINE SPECIFICATION

### Context Management Strategy

**Rolling Summaries Approach:**
- Chapters 1-5 → AI generates compressed summary (500 words)
- Keep full text of only last 2 chapters in context
- When generating Chapter 10:
  - Summary of Chapters 1-7
  - Full text of Chapters 8-9
  - Outline
  - World Bible (only relevant sections)
  - Character profiles (only characters in this chapter)

**Benefits:**
- Faster generation
- Reduced token usage
- More focused output
- No context window overload

### Validation Checkpoints

**Lightweight checks per chapter (10 seconds):**
- POV drift
- Tense consistency

**Deep validation at act breaks:**
- Chapters 5, 10, 15, 20
- Full consistency review
- Character arc alignment
- Theme resonance
- Plot coherence

### Surgical Fixes vs Full Regeneration

**If error detected:**
- Identify specific paragraph with issue
- Regenerate ONLY that section (200 words)
- Preserve the good parts

**Only regenerate full chapter if:**
- Multiple errors across chapter
- Structural problems
- Complete voice mismatch

### Story Structure Templates

Pre-built frameworks:
- **Hero's Journey** (fantasy, adventure)
- **Three-Act Structure** (thriller, mystery)
- **Romance Arc** (meet-cute → conflict → resolution)
- **Save the Cat** (commercial fiction)
- **Memoir Arc** (reflection-based structure)
- **How-To Structure** (nonfiction)

User selects template, AI adapts to their specific story.

---

## EXPORT PIPELINE SPECIFICATION

### DOCX Generation
```javascript
const docx = require('docx');

Features:
- Page size: 6×9 inches (or custom)
- Margins: 0.5" all sides
- Font: Garamond or Times New Roman, 11pt
- Line spacing: 1.15
- Chapter headers: Centered, bold
- Page numbers: Bottom center, starting after front matter
- Title page: Title, author, centered
- Copyright page: Standard KDP copyright text
- Table of contents: Auto-generated from chapter titles
```

### PDF Generation
- Convert from DOCX using pdf-lib
- Embed fonts
- Ensure print-ready quality (300 DPI minimum)
- Add bleed for print covers (0.125" all sides)

### EPUB Generation
- Proper metadata (title, author, ISBN if provided)
- Table of contents navigation
- Responsive text (adapts to screen size)
- Cover image embedded

### Cover Generation Pipeline
1. User describes cover (genre, mood, elements, colors)
2. SDXL generates 4 options (2 min total)
3. User selects favorite
4. Text overlay editor:
   - Title placement
   - Author name placement
   - Font selection
   - Color adjustment
5. Export formats:
   - Print: 6×9 with 0.125" bleed, 300 DPI PDF
   - eBook: 1600×2560 px JPG

### Metadata Generation
**Blurb (Llama 8B, 30 seconds):**
```
Write a compelling 200-word book description for Amazon KDP:

Title: [title]
Logline: [logline]
Genre: [genre]

Make it hook-focused and conversion-optimized.
```

**Keywords (Qwen 14B, 1 min):**
```
Based on this novel summary, suggest 7 searchable KDP keywords:

[manuscript summary]

Focus on specific, searchable terms (not generic words).
```

**Categories:**
AI suggests 2-3 Amazon categories based on genre/theme analysis.

---

## DATABASE SCHEMA

### SQLite Tables

```sql
CREATE TABLE projects (
  id TEXT PRIMARY KEY,
  name TEXT,
  type TEXT, -- fiction, nonfiction, memoir, journal, etc.
  created_at INTEGER,
  modified_at INTEGER,
  status TEXT -- intake, generating, editing, completed
);

CREATE TABLE lock_sheets (
  project_id TEXT PRIMARY KEY,
  project_type TEXT,
  series_structure TEXT,
  title TEXT,
  genre TEXT,
  subgenre TEXT,
  audience_level TEXT,
  voice TEXT,
  pov TEXT,
  tense TEXT,
  themes TEXT, -- JSON array
  wordcount_target INTEGER,
  chapter_count INTEGER,
  generation_mode TEXT,
  world_bible TEXT, -- JSON blob
  character_ledger TEXT, -- JSON blob
  export_formats TEXT, -- JSON array
  drift_params TEXT, -- JSON blob
  FOREIGN KEY(project_id) REFERENCES projects(id)
);

CREATE TABLE chapters (
  id TEXT PRIMARY KEY,
  project_id TEXT,
  chapter_number INTEGER,
  title TEXT,
  content TEXT,
  wordcount INTEGER,
  status TEXT, -- draft, validated, locked
  validation_notes TEXT,
  created_at INTEGER,
  FOREIGN KEY(project_id) REFERENCES projects(id)
);

CREATE TABLE outlines (
  project_id TEXT PRIMARY KEY,
  structure_template TEXT,
  full_outline TEXT,
  chapter_summaries TEXT, -- JSON array
  FOREIGN KEY(project_id) REFERENCES projects(id)
);

CREATE TABLE exports (
  id TEXT PRIMARY KEY,
  project_id TEXT,
  export_type TEXT, -- docx, pdf, epub, cover
  file_path TEXT,
  created_at INTEGER,
  FOREIGN KEY(project_id) REFERENCES projects(id)
);
```

---

## DEVELOPMENT ROADMAP

### Phase 1: Foundation (Weeks 1-2)
- Set up Electron + React app skeleton
- Integrate node-llama-cpp
- Download and test AI models (Mixtral, Qwen, Llama, SDXL)
- Create basic UI with Lamont Labs design system
- SQLite database setup

**Deliverable:** App launches, models load, test prompts work

### Phase 2: Intake Wizard (Weeks 3-5)
- Build question flow engine
- Implement adaptive branching
- Create fiction/nonfiction/memoir/journal specific wizards
- Build worldbuilding suite UI
- Character ledger builder
- Lock Sheet generation

**Deliverable:** Complete intake flow saves to database

### Phase 3: Generation Engine (Weeks 6-9)
- Implement outline generation (Qwen)
- Chapter-by-chapter generation (Mixtral)
- Validation system (Llama)
- Rolling summaries context management
- Drift guard implementation
- Progress UI with real-time updates

**Deliverable:** Generate complete novel from intake to draft

### Phase 4: Editor & Polish (Weeks 10-11)
- Build manuscript editor UI
- AI suggestion system
- Enhancement passes (sensory, emotion, dialogue, pacing)
- Surgical fix implementation

**Deliverable:** Edit and refine generated manuscripts

### Phase 5: Cover Generator (Week 12)
- SDXL integration
- Cover description interface
- Text overlay editor
- Export at print/eBook resolutions

**Deliverable:** Generate professional book covers

### Phase 6: Export Pipeline (Weeks 13-14)
- DOCX formatter with KDP-ready styling
- PDF generation
- EPUB generation
- Metadata generator (blurb, keywords)
- Package bundler (creates complete KDP folder)

**Deliverable:** One-click export to KDP-ready package

### Phase 7: Series Management (Week 15)
- Multi-book project support
- Continuity tracking
- Character/world consistency across volumes
- Timeline sync

**Deliverable:** Manage trilogy/series projects

### Phase 8: Testing & Polish (Weeks 16-18)
- Generate 10-15 test books across genres
- Bug fixes
- Performance optimization
- UI/UX refinement
- Prompt engineering improvements

**Deliverable:** Stable, reliable system

### Phase 9: Packaging (Week 19-20)
- Build standalone executable
- Bundle models with app
- Create installer
- Design app icon
- Final testing on clean system

**Deliverable:** Self-contained Greyline.exe ready to distribute

**Total Timeline: 19-20 weeks**

---

## TECHNICAL CONSIDERATIONS

### System Requirements
**Minimum:**
- CPU: 6-core modern processor (Intel i5/Ryzen 5 or better)
- RAM: 32GB
- Storage: 60GB free space
- GPU: Optional (CPU inference works, GPU accelerates)

**Recommended (NucBox K6):**
- CPU: AMD Ryzen 7 7840HS (8-core)
- RAM: 32GB DDR5
- Storage: 1TB SSD (expandable to 8TB)
- GPU: Integrated AMD Radeon

### Performance Optimization
- Load models on-demand (not all simultaneously)
- Unload inactive models to free RAM
- Use quantized models (GGUF format for efficiency)
- Implement generation queuing (prevents memory overload)
- Chapter-by-chapter saves (auto-recovery on crash)

### Error Handling
- Auto-save progress every chapter
- Resume generation from last completed section
- Model loading failure fallback
- Validation failure auto-regeneration (max 3 attempts)
- User-facing error messages (non-technical language)

### Security & Privacy
- All data stored locally (no cloud sync)
- SQLite database encryption (optional user password)
- No telemetry or analytics
- No internet connection required after setup
- User owns all generated content

---

## SUCCESS METRICS

### MVP Success Criteria
1. Generate complete 50k word novel in under 60 minutes
2. Zero POV/tense drift in 90%+ of generated chapters
3. Cover generation produces acceptable design in under 5 minutes
4. Export creates KDP-ready package without errors
5. App runs smoothly on NucBox K6 (32GB RAM)

### Quality Benchmarks
- Manuscript requires <10% editing for publication
- Character consistency maintained across all chapters
- World rules never violated
- Thematic resonance detectable in final output
- Blurb generates compelling copy 80%+ of the time

---

## FUTURE ENHANCEMENTS (Post-Launch)

### Version 2.0 Possibilities
- Add Phi-3 Medium for technical writing support
- Screenplay formatter with industry-standard layout
- Expanded template library (50+ genre templates)
- Multi-language support (Spanish, French, German manuscripts)
- Advanced analytics (reading level, pacing heat maps)
- Custom model fine-tuning on user's writing style
- Integration with ProWritingAid/Grammarly APIs (optional)

### Nice-to-Have Features
- Text-to-speech narrator for audiobook preview
- Comic book panel description mode
- Children's book illustration generator
- Marketing copy generator (Amazon ads, social posts)
- ISBN generation and tracking

---

## APPENDIX: PROMPT EXAMPLES

### Chapter Generation Prompt Template
```
You are writing Chapter [X] of "[Title]"

LOCK SHEET:
- POV: [1st/3rd person]
- Tense: [present/past]
- Voice: [voice description]
- Genre: [genre]
- Themes: [theme1, theme2, theme3]

WORLD RULES:
[relevant world bible sections]

CHARACTERS IN THIS CHAPTER:
[character profiles]

STORY CONTEXT:
Outline for this chapter: [chapter outline]
Previous chapter summary: [summary]

REQUIREMENTS:
- Write exactly 2,500 words
- Maintain [POV] [tense]
- Follow voice guidelines
- Do not violate world rules
- Advance these plot points: [points]

BEGIN CHAPTER [X]:
```

### Validation Prompt Template
```
Analyze this chapter for consistency violations:

CHAPTER TEXT:
[chapter content]

CHECK FOR:
1. POV drift (must be [POV])
2. Tense inconsistency (must be [tense])
3. Voice mismatch (reference: [voice description])
4. Character behavior inconsistency (reference: [character profiles])
5. World rule violations (reference: [world rules])

PREVIOUS CHAPTERS:
[summaries of previous chapters]

Report any violations with specific examples and line references.
```

### Blurb Generation Prompt
```
Write a compelling 200-word book description for Amazon KDP.

BOOK INFO:
Title: [title]
Genre: [genre]
Logline: [one-sentence hook]
Main character: [protagonist summary]
Central conflict: [conflict]
Stakes: [what happens if they fail]

STYLE REQUIREMENTS:
- Hook in first sentence
- Focus on conflict and stakes
- End with compelling question or teaser
- Use active voice
- Genre-appropriate tone
- Make readers want to click "Buy Now"

Write the blurb:
```

---

## CONTACT & NOTES

**Developer:** [Your Name]  
**Platform:** NucBox K6 (AMD Ryzen 7 7840HS, 32GB RAM, 1TB SSD)  
**Development Environment:** Windows/Linux  
**Target Use:** Personal KDP publishing automation  

**Philosophy:** "Obsession turned into systems."

---

**END OF MASTER SPECIFICATION**

This document contains everything needed to build Greyline-OS from concept to completion. Refer to this spec when creating the dedicated Greyline-OS Repl and beginning development.
