import org.jsoup.Jsoup;

import java.io.*;
import java.util.Scanner;
import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.*;

public class RSS {
    public static final int MAX_ITEMS = 10;

    public static void main(String[] args) {
        System.out.println("Hello there! Welcome to RSS reader\n");
        menu();

    }

    public static void menu(){

        if (InternetConnectionChecker() == true) {
            // handle invalid inputs
            try {

                Scanner scanner = new Scanner(System.in);

                int choice;

                System.out.println(
                        "choose an action:\n" +
                                "[1] Show Updates\n" +
                                "[2] add URL\n" +
                                "[3] Remove URL\n" +
                                "[4] exit\n");

                choice = scanner.nextInt();

                if (choice == 1) showUpdates();
                else if (choice == 2) addURL();
                else if (choice == 3) removeURL();
                else if (choice == 4) exit();
                else {
                    System.out.println(" enter a valid opration number. \n");
                    menu();
                }
            } catch (Exception exception) {
                System.out.println(" enter a valid opration number. \n");
                menu();
            }
        }else {
            exit();
        }
    }
    public static void addURL() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("please enter website URL to add:\n-1 to return");
        String URL = scanner.next();

        if (URL.equals(-1)) menu();

        // check if it is url must be added here
        try {

            //html
            String html = fetchPageSource(URL);

            //title
            String URLTitle = extractPageTitle(html);

            //rssURL
            String rssURL = extractRssUrl(URL);

            //add to file
            try {

                // check if file exists to open or create file
                File file = new File("data.txt");
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file, true);
                BufferedWriter writer = new BufferedWriter(fw);

                String urlInf = URLTitle + ";" + URL + ";" + rssURL + "\n";

                // add url to file if it is a new url
                if(isInFIle(URL) == false){
                    writer.write(urlInf);
                    writer.close();
                    System.out.println(URL + " added successfully");

                }else {
                    writer.close();
                    System.out.println(URL + " already exists");
                }

                // return to menu process finished
                menu();

            } catch (IOException e) {
                System.out.println("couldn't save it. try again");
                menu();
                throw new RuntimeException(e);
            }

        }catch (Exception e) {
            System.out.println("couldn't save it. try again");
            menu();
            throw new RuntimeException(e);
        }


    }
    public static void removeURL(){

        Scanner scanner = new Scanner(System.in);

        System.out.println("please enter website URL to remove:\n-1 to return");
        String URL = scanner.next();

        if (URL.equals(-1)) menu();

        //check if url exist
        if(isInFIle(URL) == true){

            try {

                // open  existed file
                File file = new File("data.txt");
                FileReader fr = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fr);

                //new temp file as tool for removing url
                File tempFile = new File("data2.txt");
                FileWriter newFw = new FileWriter(tempFile);
                BufferedWriter bufferedWriter = new BufferedWriter(newFw);

                String line;
                URL = URL.trim();

                // remove
                while ((line = bufferedReader.readLine()) != null) {

                    Boolean Contains = line.contains(URL);
                    if (Contains) continue;

                    bufferedWriter.write(line+"\n");

                }

                bufferedWriter.close();
                bufferedReader.close();

                if (file.exists()) {
                    if (file.delete()) {
                        boolean successful = tempFile.renameTo(new File("data.txt"));
                        System.out.println(URL + " removed successfully");
                        menu();


                    } else {
                        System.out.println("Try again. Failed to delete the file.");
                        menu();

                    }
                } else {
                    System.out.println("Try again. Failed to delete the file.");
                    menu();
                }

            } catch (Exception e) {
                System.out.println("Try again. Failed to delete the file.");
                menu();
                throw new RuntimeException(e);
            }

        }else {
            System.out.println("Couldn't find " + URL);
            menu();
        }


    }
    public static void showUpdates(){

            Scanner scanner = new Scanner(System.in);

            int counter = showUpdatesMenu();
            int choice = scanner.nextInt();

            try {

                //open file
                File file = new File("data.txt");

                String line, sub, rssURL, title;

                if (choice == -1) menu();
                else if (choice == 0) {
                    FileReader nfr = new FileReader(file);
                    BufferedReader nbufferedReader = new BufferedReader(nfr);
                    while ((line = nbufferedReader.readLine()) != null){

                        //extract title
                        sub = line.substring(line.indexOf(";") + 1 , line.length());

                        //extract rss url
                        rssURL = sub.substring(sub.indexOf(';') + 1, sub.length());

                        // get content
                        retrieveRssContent(rssURL);
                }
                    nbufferedReader.close();
                    System.out.println("\n Done:D \n");
                    menu();

            } else if (choice >= 1 && choice <= counter) {

                    FileReader nfr = new FileReader(file);
                    BufferedReader nbufferedReader = new BufferedReader(nfr);

                    int seconCounter = 1;

                    // get content of specific line
                    while ((line = nbufferedReader.readLine()) != null){

                        if (choice == seconCounter) {
                            title = line.substring(0, line.indexOf(";"));
                            sub = line.substring(line.indexOf(";") + 1, line.length());
                            rssURL = sub.substring(sub.indexOf(';') + 1, sub.length());

                            retrieveRssContent(rssURL);
                            System.out.println(title);
                        }
                        nbufferedReader.close();
                        seconCounter++;
                    }

                    System.out.println("\n Done:D \n");
                    menu();

            }else{
                System.out.println("Enter a valid number or -1 to return");
                    showUpdates();
                }


        }catch (Exception exception){
            throw new RuntimeException(exception);
        }

    }

    // shows every item in file as list and return the number of items
    public static int showUpdatesMenu() {

        int counter = 0;
        System.out.println("[0] All website");

        try {

            File file = new File("data.txt");
            FileReader fr = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fr);

            String line, title;

            while ((line = bufferedReader.readLine()) != null) {

                // print titles
                title = "[" + (counter+1) + "] " + line.substring(0, line.indexOf(";"));
                System.out.println(title);
                counter++;
            }

            bufferedReader.close();

            System.out.println("Enter -1 to return");

            return counter;

        }catch (Exception exception){

            throw new RuntimeException(exception);
        }

    }


    public static void exit(){
        System.exit(0);
    }
    public static Boolean isInFIle(String URL){

            try {
                File file = new File("data.txt");
                FileReader fr = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fr);

                String line;
                URL = URL.trim();

                while ((line = bufferedReader.readLine()) != null) {

                    // extract url from each line of file
                    int temp = line.indexOf(';') + 1;
                    String subline = line.substring(temp, line.length());
                    String subline2 = subline.substring(0, subline.indexOf(";"));
                    subline2 = subline2.trim();

                    if (subline2.equals(URL)) {
                        bufferedReader.close();
                        return true;
                    }

                }

            } catch (Exception e) {

                throw new RuntimeException(e);
            }

            return false;


    }

    // Extract page title from given url
public static String extractPageTitle(String html)
{
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            //System.out.println(doc);
            //System.out.println(x);
            return doc.select("title").first().text();
        }
        catch (Exception e) {
            return "Error: no title tag found in page source!";
        }
}

    public static void retrieveRssContent(String rssUrl)
    {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).
                            getTextContent());
                    }
                }
            }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
            }
        }

        public static String fetchPageSource(String urlString) throws Exception
        {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
        }

        private static String toString(InputStream inputStream) throws IOException
        {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
        }
    public static String extractRssUrl(String url) throws IOException {

        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }


    public static boolean InternetConnectionChecker() {
            try {
                InetAddress inetAddress = InetAddress.getByName("www.google.com");
                boolean isConnected = inetAddress.isReachable(5000); // 5000 milliseconds timeout
                if (isConnected) {
                    System.out.println("Connected to the internet.");
                    return true;
                } else {
                    System.out.println("Not connected to the internet.");
                    return false;
                }
            } catch (UnknownHostException e) {
                System.out.println("Not connected to the internet.");
                return false;
            } catch (IOException e) {
                System.out.println("Not connected to the internet. please try again");
                return false;
            }

    }
}