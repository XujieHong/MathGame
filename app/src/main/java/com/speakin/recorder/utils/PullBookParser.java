package com.speakin.recorder.utils;

/**
 * Created by hongxujie on 1/19/18.
 */

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class PullBookParser implements BookParser {

    @Override
    public List<Book> parse(String xmlStr) throws Exception {
        List<Book> books = null;
        Book book = null;

//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//      XmlPullParser parser = factory.newPullParser();

        XmlPullParser parser = Xml.newPullParser(); //由android.util.Xml创建一个XmlPullParser实例
        parser.setInput(new StringReader(xmlStr));               //设置输入流 并指明编码方式

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    books = new ArrayList<Book>();
                    break;
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("book")) {
                        book = new Book();
                    } else if (parser.getName().equals("x")) {
                        eventType = parser.next();
                        book.setX(Integer.parseInt(parser.getText()));
                    } else if (parser.getName().equals("y")) {
                        eventType = parser.next();
                        book.setY(Integer.parseInt(parser.getText()));
                    } else if (parser.getName().equals("z")) {
                        eventType = parser.next();
                        book.setZ(Integer.parseInt(parser.getText()));
                    } else if (parser.getName().equals("formula")) {
                        eventType = parser.next();
                        book.setFormula(parser.getText());
                    } else if (parser.getName().equals("isMaster")) {
                        eventType = parser.next();
                        book.setScore(Integer.parseInt(parser.getText()));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("book")) {
                        books.add(book);
                        book = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        return books;
    }

    @Override
    public String serialize(List<Book> books) throws Exception {
//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//      XmlSerializer serializer = factory.newSerializer();

        XmlSerializer serializer = Xml.newSerializer(); //由android.util.Xml创建一个XmlSerializer实例
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);   //设置输出方向为writer
        serializer.startDocument("UTF-8", true);
        serializer.startTag("", "books");
        for (Book book : books) {
            serializer.startTag("", "book");

            serializer.startTag("", "x");
            serializer.text(book.getX() + "");
            serializer.endTag("", "x");
            serializer.startTag("", "y");
            serializer.text(book.getY() + "");
            serializer.endTag("", "y");
            serializer.startTag("", "z");
            serializer.text(book.getZ() + "");
            serializer.endTag("", "z");

            serializer.startTag("", "formula");
            serializer.text(book.getFormula());
            serializer.endTag("", "formula");

            serializer.startTag("", "isMaster");
            serializer.text(book.getScore() + "");
            serializer.endTag("", "isMaster");

            serializer.endTag("", "book");
        }
        serializer.endTag("", "books");
        serializer.endDocument();

        return writer.toString();
    }
}