package com.jujujuijk.android.asynctask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.fragment.ShowFeedFragment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

public class FeedParser extends
        AsyncTask<Integer, Void, List<Bundle>> {

    protected RssParserCallBack m_parent = null;

    protected static final int NB_MAX_LOAD = ShowFeedFragment.NB_MAX_ITEMS;

    protected Feed mFeed;
    protected URL mUrl;

    private Document mDoc;
    private Element mRoot;
    private NodeList mNodes;

    public FeedParser(RssParserCallBack parent, Feed feed) {
        m_parent = parent;
        mFeed = feed;
        try {
            mUrl = new URL(mFeed.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Bundle> doInBackground(Integer... nbMax) {
        List<Bundle> ret = null;

        if (mUrl == null)
            return ret;

        try {
            mDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(mUrl.openStream());
            mRoot = mDoc.getDocumentElement();
            mNodes = mRoot.getElementsByTagName("item");

            for (int i = 0; i < mNodes.getLength() && i < NB_MAX_LOAD && i < nbMax[0]; ++i) {
                try {
                    Element elem = (Element) mNodes.item(i);
                    NodeList desc = elem.getElementsByTagName("description");
                    NodeList date = elem.getElementsByTagName("pubDate");
                    NodeList title = elem.getElementsByTagName("title");
                    NodeList link = elem.getElementsByTagName("link");
                    NodeList author = elem.getElementsByTagName("author");
                    NodeList category = elem.getElementsByTagName("category");
                    NodeList enclosure = elem.getElementsByTagName("enclosure");

                    if (desc.getLength() != 1 || title.getLength() != 1)
                        continue;

                    if (ret == null)
                        ret = new ArrayList<Bundle>();

                    String strTitle = title.item(0).getTextContent();
                    String strDesc = desc.item(0).getTextContent();
                    String imageUrl = null;
                    String strDate = null;
                    String strLink = null;
                    String strAuthor = null;
                    String strCategory = null;

                    if (date.getLength() > 0)
                        strDate = (String) date.item(0).getTextContent().subSequence(5, 25);
                    if (author.getLength() > 0)
                        strAuthor = author.item(0).getTextContent();
                    if (link.getLength() > 0)
                        strLink = link.item(0).getTextContent();
                    if (category.getLength() > 0)
                        strCategory = category.item(0).getTextContent();
                    if (enclosure.getLength() > 0) {
                        Element tmp = (Element) enclosure.item(0);
                        if (tmp.getAttribute("type").contains("image"))
                            imageUrl = tmp.getAttribute("url");
                    }

                    // No image in 'enclosure' tag, try to find one in the description
                    if (imageUrl == null) {
                        final Pattern ptrn = Pattern.compile("<img src=\"(.+?)\"/>");
                        final Matcher mtchr = ptrn.matcher(strDesc);
                        if (mtchr.find()) {
                            imageUrl = strDesc.substring(mtchr.start(), mtchr.end()).replace("<img src=\"", "").replace("\"/>", "");
                        }
                    }


                    Bundle b = new Bundle();
                    b.putInt("id", i);
                    b.putString("feed", mFeed.getName());
                    b.putString("title", strTitle);
                    b.putString("description", strDesc);

                    if (strLink != null)
                        b.putString("link", strLink);
                    if (strAuthor != null)
                        b.putString("author", strAuthor);
                    if (strCategory != null)
                        b.putString("category", strCategory);
                    if (strDate != null)
                        b.putString("date", strDate);
                    if (imageUrl != null)
                        b.putString("imageUrl", imageUrl);

                    ret.add(b);
                } catch (Exception e) {
                    e.printStackTrace();
                } // Expecting here for <item>'s that do not contain images
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    /**
     * The system calls this to perform work in the UI thread and delivers the
     * result from doInBackground()
     */

    protected void onPostExecute(List<Bundle> result) {
        if (m_parent == null)
            return;
        if (m_parent instanceof Fragment && (((Fragment) m_parent).isRemoving()
                || ((Fragment) m_parent).isDetached() || ((Fragment) m_parent).getActivity() == null))
            return;
        m_parent.onRssParserPostExecute(result, mFeed);
    }

    public interface RssParserCallBack {
        abstract void onRssParserPostExecute(List<Bundle> images, Feed feed);
    }
}
