package com.speakin.recorder.utils;

/**
 * Created by hongxujie on 1/19/18.
 */

import java.io.InputStream;
import java.util.List;

public interface BookParser {
    /**
     * 解析输入流 得到Book对象集合
     * @param xmlStr
     * @return
     * @throws Exception
     */
    public List<Book> parse(String xmlStr) throws Exception;

    /**
     * 序列化Book对象集合 得到XML形式的字符串
     * @param books
     * @return
     * @throws Exception
     */
    public String serialize(List<Book> books) throws Exception;
}