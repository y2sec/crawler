package naverNewsRss;


import naverNews.Clustering;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class RSSFeedParser {
    static final String TITLE = "title";
    static final String DESCRIPTION = "description";
    static final String THUMBNAIL = "thumbnail";
    static final String LANGUAGE = "language";
    static final String COPYRIGHT = "copyright";
    static final String LINK = "link";
    static final String AUTHOR = "author";
    static final String ITEM = "item";
    static final String PUBDATE = "pubDate";
    static final String CATEGORY = "category";
    Clustering clustering = new Clustering();

    final URL url;

    public RSSFeedParser(String feedUrl) throws MalformedURLException {
        this.url = new URL(feedUrl);
    }

    public Feed readFeed() {
        Feed feed = null;
        try {
            boolean isFeedHeader = true;
            String description = "";
            String title = "";
            String link = "";
            String language = "";
            String copyright = "";
            String author = "";
            String pubdate = "";
            String category = "";
            String thumbnail = "";

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();

            InputStream in = read();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    String localPart = event.asStartElement().getName().getLocalPart();
                    switch (localPart) {
                        case ITEM:
                            if (isFeedHeader) {
                                isFeedHeader = false;
                                feed = new Feed(title, link, description, language, copyright, pubdate);
                            }
                            event = eventReader.nextEvent();
                            break;
                        case TITLE:
                            title = getCharacterDate(event, eventReader);
                            break;
                        case DESCRIPTION:
                            description = getCharacterDate(event, eventReader);
                            break;
                        case LINK:
                            link = getCharacterDate(event, eventReader);
                            break;
                        case CATEGORY:
                            category = getCharacterDate(event, eventReader);
                            break;
                        case LANGUAGE:
                            language = getCharacterDate(event, eventReader);
                            break;
                        case AUTHOR:
                            author = getCharacterDate(event, eventReader);
                            break;
                        case COPYRIGHT:
                            copyright = getCharacterDate(event, eventReader);
                            break;
                        case PUBDATE:
                            pubdate = getCharacterDate(event, eventReader);
                            break;
                        case THUMBNAIL:
                            thumbnail = getCharacterDate(event, eventReader);
                            @SuppressWarnings("unchecked") Iterator<Attribute> attribute = event.asStartElement().getAttributes();
                            while (attribute.hasNext()) {
                                Attribute myAttribute = attribute.next();
                                if (myAttribute.getName().toString().equals("url")) {
                                    thumbnail = myAttribute.getValue();
                                }
                            }
                            break;
                    }
                } else if (event.isEndElement()) {
                    if (event.asEndElement().getName().getLocalPart().equals(ITEM)) {
                        boolean insert = true;
                        for (FeedMessage message : feed.getMessages()) {
                            double similarity = clustering.jaccard(message.getTitle(),title);
                            if (similarity > 0.1) {
                                insert = false;
                                break;
                            }
                        }
                        if (!insert)
                            continue;

                        FeedMessage message = new FeedMessage();
                        message.setAuthor(author);
                        message.setDescription(description);
                        message.setCategory(category);
                        message.setLink(link);
                        message.setTitle(title);
                        message.setPubdate(pubdate);
                        message.setThumbnail(thumbnail);
                        feed.getMessages().add(message);
                        event = eventReader.nextEvent();
                        continue;
                    }
                }
            }
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
        return feed;
    }

    private InputStream read() throws IOException {
        return url.openStream();
    }

    private String getCharacterDate(XMLEvent event, XMLEventReader eventReader) throws XMLStreamException {
        String result = "";

        do {
            event = eventReader.nextEvent();
            if (event instanceof Characters) {
                result += event.asCharacters().getData();
            }
        }
        while (!eventReader.peek().isEndElement());

        return result;
    }
}