package com.example.csvspring.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.LinkedList;

@Converter
@Slf4j
public class ConverterTemp implements AttributeConverter<LinkedList<Double>, byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(LinkedList<Double> doubles) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(doubles);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("IOException while converting LinkedList<Double> to byte[]");
            return null;
        }
    }

    @Override
    public LinkedList<Double> convertToEntityAttribute(byte[] bytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

            return (LinkedList<Double>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("IOException or ClassNotFoundException while converting byte[] to LinkedList<Double>");
            return null;
        }
    }
}
