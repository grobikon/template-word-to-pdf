package ru.grobikon.templatewordtopdf.schema;

import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "root")
public class TestXmlSchema {
    private String name;
    private String text;
}
