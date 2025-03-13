import sys
from PyPDF2 import PdfReader, PdfWriter
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import letter
from io import BytesIO

def sign_pdf(pdf_path, signature_number):
    output_pdf_path = pdf_path  # Overwrite original file

    # Read the existing PDF
    reader = PdfReader(pdf_path)
    writer = PdfWriter()

    # Create a signature overlay
    packet = BytesIO()
    can = canvas.Canvas(packet, pagesize=letter)
    can.setFont("Helvetica-Bold", 12)
    can.drawString(50, 750, f"SIGNED BY {signature_number}")
    can.save()

    # Move to the beginning of the StringIO buffer
    packet.seek(0)
    new_pdf = PdfReader(packet)

    # Merge signature onto first page
    first_page = reader.pages[0]
    first_page.merge_page(new_pdf.pages[0])

    # Add all pages to writer
    writer.add_page(first_page)
    for i in range(1, len(reader.pages)):
        writer.add_page(reader.pages[i])

    # Save the new signed PDF
    with open(output_pdf_path, "wb") as output_file:
        writer.write(output_file)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python sign_pdf.py <pdf_path> <signature_number>")
        sys.exit(1)

    pdf_path = sys.argv[1]
    signature_number = sys.argv[2]

    sign_pdf(pdf_path, signature_number)
