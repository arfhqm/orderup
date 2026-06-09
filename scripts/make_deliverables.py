from pathlib import Path


def pdf_escape(text):
    return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


def build_pdf(path, title, sections):
    page_width = 595
    page_height = 842
    margin = 56
    line_height = 16
    pages = []
    current = []
    y = page_height - margin

    def add_line(text, size=11, gap=0):
        nonlocal y, current
        if y < margin + 40:
            pages.append(current)
            current = []
            y = page_height - margin
        current.append((text, size, y))
        y -= line_height + gap

    add_line(title, 22, 10)
    for heading, lines in sections:
        add_line(heading, 15, 4)
        for line in lines:
            chunks = [line[i:i + 92] for i in range(0, len(line), 92)] or [""]
            for chunk in chunks:
                add_line(chunk, 10)
        y -= 8

    if current:
        pages.append(current)

    objects = []
    catalog_id = 1
    pages_id = 2
    font_id = 3
    next_id = 4
    page_ids = []

    for page in pages:
        page_id = next_id
        content_id = next_id + 1
        next_id += 2
        page_ids.append(page_id)

        stream_lines = ["BT"]
        for text, size, line_y in page:
            stream_lines.append(f"/F1 {size} Tf")
            stream_lines.append(f"{margin} {line_y} Td")
            stream_lines.append(f"({pdf_escape(text)}) Tj")
            stream_lines.append(f"-{margin} -{line_y} Td")
        stream_lines.append("ET")
        stream = "\n".join(stream_lines).encode("latin-1", "replace")

        objects.append((page_id, f"<< /Type /Page /Parent {pages_id} 0 R /MediaBox [0 0 {page_width} {page_height}] /Resources << /Font << /F1 {font_id} 0 R >> >> /Contents {content_id} 0 R >>".encode()))
        objects.append((content_id, b"<< /Length " + str(len(stream)).encode() + b" >>\nstream\n" + stream + b"\nendstream"))

    kids = " ".join(f"{page_id} 0 R" for page_id in page_ids)
    objects.insert(0, (font_id, b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"))
    objects.insert(0, (pages_id, f"<< /Type /Pages /Kids [{kids}] /Count {len(page_ids)} >>".encode()))
    objects.insert(0, (catalog_id, f"<< /Type /Catalog /Pages {pages_id} 0 R >>".encode()))
    objects.sort(key=lambda pair: pair[0])

    output = [b"%PDF-1.4\n"]
    offsets = [0]
    for object_id, body in objects:
        offsets.append(sum(len(part) for part in output))
        output.append(f"{object_id} 0 obj\n".encode())
        output.append(body)
        output.append(b"\nendobj\n")

    xref_offset = sum(len(part) for part in output)
    output.append(f"xref\n0 {len(objects) + 1}\n".encode())
    output.append(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        output.append(f"{offset:010d} 00000 n \n".encode())
    output.append(f"trailer\n<< /Size {len(objects) + 1} /Root {catalog_id} 0 R >>\nstartxref\n{xref_offset}\n%%EOF".encode())

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b"".join(output))


root = Path(__file__).resolve().parents[1]

build_pdf(
    root / "docs" / "pitch.pdf",
    "OrderUp Café POS - Pitch",
    [
        ("Problem", [
            "Small cafés often juggle handwritten tickets, edited orders, split payments, and kitchen updates manually.",
            "Generic POS demos usually look polished but skip the awkward real-life cases that staff actually handle."
        ]),
        ("Solution", [
            "OrderUp is a compact Angular + Spring Boot + MySQL POS for café service.",
            "Cashiers can take and edit orders, kitchen staff can progress ticket status, and bills can be paid or split."
        ]),
        ("AI Feature", [
            "The cart-based add-on recommender checks the live cart against the real menu and suggests a missing category.",
            "It works locally without an API key, and can optionally use OpenAI to polish the wording."
        ]),
        ("Why It Is Explainable", [
            "The backend has simple entities: MenuItem, Order, OrderItem, and Payment.",
            "Business logic lives in one OrderService and one AiRecommendationService.",
            "The Angular frontend is one focused POS screen backed by a small API service."
        ]),
        ("Demo Flow", [
            "Start MySQL with docker compose, run Spring Boot, then run Angular.",
            "Add Latte and Croissant, accept the AI add-on suggestion, send to kitchen, move status, then split payment."
        ])
    ]
)

build_pdf(
    root / "docs" / "screenshots.pdf",
    "OrderUp Café POS - Live Screenshot Checklist",
    [
        ("Screen 1 - Cashier Menu", [
            "Left panel shows seeded café menu grouped by Drinks, Food, and Desserts.",
            "Tapping any item adds it to the active cart."
        ]),
        ("Screen 2 - Cart and AI Add-on", [
            "Middle panel captures table number, order type, payment method, quantities, and kitchen notes.",
            "AI add-on card appears after cart changes and shows the recommendation source."
        ]),
        ("Screen 3 - Kitchen Board", [
            "Right panel lists live orders with item notes and status chips.",
            "Next Status moves tickets through PENDING, PREPARING, READY, and COMPLETED."
        ]),
        ("Screen 4 - Payments and Split Bills", [
            "Pay captures the remaining amount as one payment.",
            "Split 2 and Split 3 divide the remaining bill into multiple Payment records."
        ]),
        ("Capture Instructions", [
            "Run the app at http://localhost:4200, capture the four screens above, and export them into this PDF for final submission.",
            "This file is intentionally a checklist, because real screenshots should be captured from the live app after MySQL, Spring Boot, and Angular are running."
        ])
    ]
)

print("Created docs/pitch.pdf and docs/screenshots.pdf")
