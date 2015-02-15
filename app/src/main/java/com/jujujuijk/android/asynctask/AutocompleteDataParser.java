package com.jujujuijk.android.asynctask;

import android.os.AsyncTask;

import com.jujujuijk.android.rssreader.ApplicationContextProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AutocompleteDataParser extends AsyncTask<String, Void, Void> {

    ArrayList<String> mNameList;
    ArrayList<String> mUrlList;

    public AutocompleteDataParser(ArrayList<String> nameList,
                                  ArrayList<String> urlList) {
        mNameList = nameList;
        mUrlList = urlList;
    }

    @Override
    protected Void doInBackground(String... unused) {
        try {
            InputStream is = ApplicationContextProvider.getContext()
                    .getResources().getAssets().open("datas.xml");

            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("item");

            for (int i = 0; i < nodes.getLength(); ++i) {
                Element elem = (Element) nodes.item(i);
                NodeList name = elem.getElementsByTagName("name");
                NodeList url = elem.getElementsByTagName("url");

                if (name.getLength() != 1 || url.getLength() != 1)
                    continue;

                String strName = name.item(0).getTextContent();
                String strUrl = url.item(0).getTextContent();

                mNameList.add(strName);
                mUrlList.add(strUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
