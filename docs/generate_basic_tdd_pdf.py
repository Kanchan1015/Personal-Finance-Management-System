from html.parser import HTMLParser
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "FinPilot_Technical_Design_Document_Basic.html"
OUTPUT = ROOT / "FinPilot_Technical_Design_Document_Basic.pdf"


class SectionParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.sections = []
        self.current = None
        self.capture = False

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == "section":
            self.current = []
            self.capture = True
        if self.capture and tag in {"h1", "h2", "h3", "p", "li", "td", "th", "div"}:
            self.current.append("\n")

    def handle_endtag(self, tag):
        if tag == "section" and self.capture:
            text = " ".join("".join(self.current).split())
            text = re.sub(r"\s+([,.;:])", r"\1", text)
            self.sections.append(text)
            self.current = None
            self.capture = False
        elif self.capture and tag in {"h1", "h2", "h3", "p", "li", "tr", "div"}:
            self.current.append("\n")

    def handle_data(self, data):
        if self.capture and self.current is not None:
            self.current.append(data)


def ascii_clean(text):
    replacements = {
        "\u2019": "'",
        "\u2018": "'",
        "\u201c": '"',
        "\u201d": '"',
        "\u2013": "-",
        "\u2014": "-",
        "\u2022": "-",
        "\u2192": "->",
    }
    for old, new in replacements.items():
        text = text.replace(old, new)
    return text.encode("latin-1", "replace").decode("latin-1")


def wrap_text(text, width=88):
    words = text.split()
    lines = []
    line = ""
    for word in words:
        if len(line) + len(word) + 1 > width:
            if line:
                lines.append(line)
            line = word
        else:
            line = word if not line else f"{line} {word}"
    if line:
        lines.append(line)
    return lines


def pdf_escape(text):
    return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


def make_page_stream(section_text, page_number):
    section_text = ascii_clean(section_text)
    lines = wrap_text(section_text, width=92)
    max_lines = 47
    shown = lines[:max_lines]
    if len(lines) > max_lines:
        shown[-1] = shown[-1][:80] + " ..."

    commands = [
        "BT",
        "/F1 10 Tf",
        "50 790 Td",
        "14 TL",
    ]
    for i, line in enumerate(shown):
        if i == 0:
            commands.append("/F2 15 Tf")
            commands.append(f"({pdf_escape(line[:86])}) Tj")
            commands.append("T*")
            commands.append("/F1 10 Tf")
        else:
            commands.append(f"({pdf_escape(line)}) Tj")
            commands.append("T*")
    commands.append("/F1 9 Tf")
    commands.append("0 -16 Td")
    commands.append(f"(Page {page_number}) Tj")
    commands.append("ET")
    return "\n".join(commands).encode("latin-1")


def build_pdf(sections):
    objects = []
    objects.append(b"<< /Type /Catalog /Pages 2 0 R >>")

    page_count = len(sections)
    page_object_numbers = [3 + i * 2 for i in range(page_count)]
    kids = " ".join(f"{n} 0 R" for n in page_object_numbers)
    objects.append(f"<< /Type /Pages /Kids [{kids}] /Count {page_count} >>".encode("latin-1"))

    for idx, section in enumerate(sections, start=1):
        page_obj_num = 3 + (idx - 1) * 2
        stream_obj_num = page_obj_num + 1
        stream = make_page_stream(section, idx)
        page = (
            f"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
            f"/Resources << /Font << /F1 {3 + page_count * 2} 0 R /F2 {4 + page_count * 2} 0 R >> >> "
            f"/Contents {stream_obj_num} 0 R >>"
        ).encode("latin-1")
        objects.append(page)
        objects.append(b"<< /Length " + str(len(stream)).encode("latin-1") + b" >>\nstream\n" + stream + b"\nendstream")

    objects.append(b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")
    objects.append(b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>")

    output = bytearray(b"%PDF-1.4\n")
    offsets = [0]
    for number, obj in enumerate(objects, start=1):
        offsets.append(len(output))
        output.extend(f"{number} 0 obj\n".encode("latin-1"))
        output.extend(obj)
        output.extend(b"\nendobj\n")

    xref_pos = len(output)
    output.extend(f"xref\n0 {len(objects) + 1}\n".encode("latin-1"))
    output.extend(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        output.extend(f"{offset:010d} 00000 n \n".encode("latin-1"))
    output.extend(
        (
            f"trailer\n<< /Size {len(objects) + 1} /Root 1 0 R >>\n"
            f"startxref\n{xref_pos}\n%%EOF\n"
        ).encode("latin-1")
    )
    return bytes(output)


def main():
    parser = SectionParser()
    parser.feed(SOURCE.read_text(encoding="utf-8"))
    sections = [section for section in parser.sections if section.strip()]
    OUTPUT.write_bytes(build_pdf(sections))
    print(f"Wrote {OUTPUT} with {len(sections)} pages")


if __name__ == "__main__":
    main()
