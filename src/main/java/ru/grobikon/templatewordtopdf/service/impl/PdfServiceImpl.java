package ru.grobikon.templatewordtopdf.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;
import ru.grobikon.templatewordtopdf.dto.PdfResponseDto;
import ru.grobikon.templatewordtopdf.dto.TestTemplateRequestDto;
import ru.grobikon.templatewordtopdf.schema.TestXmlSchema;
import ru.grobikon.templatewordtopdf.service.PdfService;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String TEMPLATES_DIR_NAME = "templates";
    private static final String TEMPLATES_GENERATED_DIR_NAME = "generated";
    private static final String TEMPLATES_DIR_PATH = initTemplateDir();
    private static final String TEMPLATES_DIR_GENERATED_DOCS_PATH = initTemplateGeneratedDir();

    @Override
    public PdfResponseDto createTestTemplatePdf(TestTemplateRequestDto request) {
        log.debug("Start createTestTemplatePdf(), request: {}", request);
        var fileTemplate = getDocxTemplate();
        var xmlDataForFileTemplate = testToXml(toTestXml(request));
        var pathCreatedWordDoc = createWordFromTemplate(fileTemplate, xmlDataForFileTemplate);
        var pathCreatedPdfDoc = createPdfFromWordDoc(pathCreatedWordDoc);

        var pdfDoc = new File(pathCreatedPdfDoc);
        var pdfDocBase64 = "Ошибка";
        try {
            var encoded = Base64.encodeBase64(org.apache.commons.io.FileUtils.readFileToByteArray(pdfDoc));
            pdfDocBase64 = new String(encoded, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return PdfResponseDto.builder()
                .pdfBase64(pdfDocBase64)
                .build();
    }

    /**
     * Создаем документ docx. На основе шаблона и данных которые передали в request.
     * @param fileTemplate - файл шаблон который будем заполнять данными
     * @param xmlDataForFileTemplate - данные которые пудем передавать в шаблон
     * @return вернём путь имя docs файла, который будет создан
     */
    private String createWordFromTemplate(File fileTemplate, String xmlDataForFileTemplate) {
        var docName = "test.docx";
        var docPathNewFile = getDocPath(docName);
        try {
            WordprocessingMLPackage wordMLPackage = Docx4J.load(fileTemplate);
            Docx4J.bind(wordMLPackage, xmlDataForFileTemplate, Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML | Docx4J.FLAG_BIND_REMOVE_SDT);
            Docx4J.save(wordMLPackage, new File(docPathNewFile), Docx4J.FLAG_NONE);
        } catch (Docx4JException e) {
            log.error(e.getMessage(), e);
        }
        return docPathNewFile;
    }

    /**
     * Создаём PDF документ на основе word документа
     * @param pathNewWordDoc - путь имени docs файла, который будет создан
     * @return вернём путь имя pdf файла, который будет создан
     */
    private String createPdfFromWordDoc(String pathNewWordDoc) {
        var pdfName = "test.pdf";
        var pdfPathNewFile = getDocPath(pdfName);
        try {
            WordprocessingMLPackage wordMLPackageToPdf = WordprocessingMLPackage.load(new File(String.valueOf(pathNewWordDoc)));
            FileOutputStream os = new FileOutputStream(pdfPathNewFile);
            Docx4J.toPDF(wordMLPackageToPdf, os);
            os.flush();
            os.close();
        } catch (Docx4JException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return pdfPathNewFile;
    }

    private String testToXml(TestXmlSchema xmlSchema) {
        StringWriter sw = new StringWriter();
        JAXB.marshal(xmlSchema, sw);
        return sw.toString();
    }

    private static String jaxbObjectToXML(TestXmlSchema xmlSchema) {
        try {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(TestXmlSchema.class);

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            //Print XML String to Console
            StringWriter sw = new StringWriter();

            //Write XML to StringWriter
            jaxbMarshaller.marshal(xmlSchema, sw);

            //Verify XML Content
            String xmlContent = sw.toString();
            System.out.println( xmlContent );
            return xmlContent;
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private TestXmlSchema toTestXml(TestTemplateRequestDto request) {
        return TestXmlSchema.builder()
                .name(request.getName())
                .text(request.getText())
                .build();
    }

    /**
     * Получаем подготовленный шаблон, который будем заполнять значениями из request.
     */
    private File getDocxTemplate() {
        InputStream initialStream = getClass().getClassLoader().getResourceAsStream("test_template.docx");
        String savePath = TEMPLATES_DIR_PATH + File.separator + "test_template.docx";
        File targetFile = new File(savePath);
        try {
            FileUtils.copyInputStreamToFile(initialStream, targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(savePath);
    }

    public static String getDocPath(String fileName) {
        String path;
        if (TEMPLATES_DIR_GENERATED_DOCS_PATH.endsWith(File.separator)) {
            path = TEMPLATES_DIR_GENERATED_DOCS_PATH + fileName;
        } else {
            path = TEMPLATES_DIR_GENERATED_DOCS_PATH + File.separator + fileName;
        }
        return path;
    }

    private static String initTemplateDir() {
        String path;
        if (TMP_DIR.endsWith(File.separator)) {
            path = TMP_DIR + TEMPLATES_DIR_NAME;
        } else {
            path = TMP_DIR + File.separator + TEMPLATES_DIR_NAME;
        }
        File templateDir = new File(path);
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }
        return path;
    }

    private static String initTemplateGeneratedDir() {
        String path;
        if (TEMPLATES_DIR_PATH.endsWith(File.separator)) {
            path = TEMPLATES_DIR_PATH + TEMPLATES_GENERATED_DIR_NAME;
        } else {
            path = TEMPLATES_DIR_PATH + File.separator + TEMPLATES_GENERATED_DIR_NAME;
        }
        File templateGenDir = new File(path);
        if (!templateGenDir.exists()) {
            templateGenDir.mkdirs();
        }
        return path;
    }
}
