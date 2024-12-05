package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
public class PdfConvertor {

  public static void main(String[] args) {
    final String fileName = "a.pdf";
    final String filePathWithoutExt = "/home/examples/" + FilenameUtils.getBaseName(fileName);

    final PdfConvertor app = new PdfConvertor();
    app.pdfToPng(new File(filePathWithoutExt + ".pdf"));
    app.pngToPdf(new File(filePathWithoutExt + ".png"), new File(filePathWithoutExt + "_new.pdf"));
  }

  public void pdfToPng(final File srcFile, final File destFile) {
    try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(srcFile))) {
      log.debug("Pages: {}", document.getPages().getCount());

      // PDF 렌더러 생성
      final PDFRenderer pdfRenderer = new PDFRenderer(document);

      // 첫 번째 페이지를 이미지로 변환
      final BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300);

      // 이미지 형식에 맞는 파일로 저장 (PNG 또는 JPG)
      ImageIO.write(bufferedImage, "PNG", destFile);  // JPG로 변경하려면 "JPG"로 변경

      log.info("Success: pdf -> png");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void pngToPdf(final File srcFile, final File destFile) {
    try (PDDocument document = new PDDocument()) {
      final PDPage page = new PDPage();
      document.addPage(page);

      // PDF 페이지 크기 가져오기
      final float pageWidth = page.getMediaBox().getWidth();    // 페이지 너비
      final float pageHeight = page.getMediaBox().getHeight();  // 페이지 높이

      // PNG 이미지 로드
      // final PDImageXObject image = PDImageXObject.createFromFile(srcFile.getCanonicalPath(), document);
      final PDImageXObject image = PDImageXObject.createFromFileByExtension(srcFile, document);
      // 이미지 원본 크기 가져오기
      final float imageWidth = image.getWidth();
      final float imageHeight = image.getHeight();

      // 비율 계산
      float scale = Math.min(pageWidth / imageWidth, pageHeight / imageHeight);

      // 이미지의 새 크기 계산
      float scaledWidth = imageWidth * scale;
      float scaledHeight = imageHeight * scale;

      // 페이지에 이미지 삽입
      try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        contentStream.drawImage(
                image, (pageWidth - scaledWidth) / 2, (pageHeight - scaledHeight) / 2, scaledWidth, scaledHeight);
      }

      // PDF 파일 저장
      document.save(destFile);

      log.info("Success: png -> pdf");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void pdfToPng(final File srcFile) {
    pdfToPng(srcFile, changeExtension(srcFile, "png"));
  }

  public void pngToPdf(final File srcFile) {
    pngToPdf(srcFile, changeExtension(srcFile, "pdf"));
  }

  private File changeExtension(final File file, final String newExt) {
    return file.toPath().resolveSibling(FilenameUtils.getBaseName(file.getName()) + "." + newExt).toFile();
  }
}
