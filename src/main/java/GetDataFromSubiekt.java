import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.*;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 *
 * @author rafalo
 */
public class GetDataFromSubiekt {
    String folderXml="c:/sklepolsh__/";
    String folder="c:/sklepolsh/files__/";
    String db_connect_string="jdbc:sqls__erver://rafalo.webhop.me\\InsERTGT;databaseName=Olsh";
    String db_userid="sa__";
    String db_password="__";

    public String getFolderXml() {
        return folderXml;
    }

    public void setFolderXml(String folderXml) {
        this.folderXml = folderXml;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getDb_connect_string() {
        return db_connect_string;
    }

    public void setDb_connect_string(String db_connect_string) {
        this.db_connect_string = db_connect_string;
    }

    public String getDb_userid() {
        return db_userid;
    }

    public void setDb_userid(String db_userid) {
        this.db_userid = db_userid;
    }

    public String getDb_password() {
        return db_password;
    }

    public void setDb_password(String db_password) {
        this.db_password = db_password;
    }

    public GetDataFromSubiekt(String propertiesFilename) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propertiesFilename));


 /*           String folderXml="c:/sklepolsh/";
            String folder="c:/sklepolsh/files/";
            String db_connect_string="jdbc:sqlserver://rafalo.webhop.me\\InsERTGT;databaseName=Olsh";
            String db_userid="sa";
            String db_password="";
   */
            setFolderXml(props.getProperty("folderXml").trim());
            setFolder(props.getProperty("folder").trim());
            setDb_connect_string(props.getProperty("db_connect_string").trim());
            setDb_userid(props.getProperty("db_userid").trim());
            setDb_password(props.getProperty("db_password").trim());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String toPrettyURL(String string) {
        return Normalizer.normalize(string.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^\\p{Alnum}]+", "_");
    }

    public static String cleanTagPerservingLineBreaks(String html) {
        String result = "";
        if (html == null)
            return html;

        String out=html;

        html=html.replaceAll("(?i)<br[^>]*>", "br2n");
        html.replaceAll("(?i)<p[^>]*>", "br2n");
        html.replaceAll("(?i)<li[^>]*>", "br2n");
        html.replaceAll("(?i)<div[^>]*>", "br2n");
        html.replaceAll("(?i)<tr[^>]*>", "br2n");

        result = Jsoup.parse(html).text();
        result = result.replaceAll("br2n", "\n");

        return result ;
    }

    public void dbConnect(boolean onlyStan)
    {
        try {
            FileWriter fstream = new FileWriter(folder+"export.csv");
            // root elements
            BufferedWriter out = new BufferedWriter(fstream);


            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(db_connect_string,db_userid,db_password);

        //    <property name="javax.persistence.jdbc.url" value="jdbc:sqlserver://rafalo.webhop.me\InsERTGT;databaseName=Olsh"/>
         //   <property name="javax.persistence.jdbc.user" value="sa"/>
      //      <property name="javax.persistence.jdbc.password" value=""/>
      //      <property name="javax.persistence.jdbc.driver" value="com.microsoft.sqlserver.jdbc.SQLServerDriver"/>

            System.out.println("connected");
            //    conn.setAutoCommit(false);
            Statement statement = conn.createStatement();


            //KATEGORIE
            HashMap<String, String> cetMap = new HashMap<String, String>();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element categoryElement = doc.createElement("kategorie");
            doc.appendChild(categoryElement);
            Element category = doc.createElement("kategoria");
            String queryString = "select distinct grt_Nazwa,grt_Id from sl_GrupaTw"; // where type='u'
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                //String test=rs.getString("grt_Nazwa");
                String test=new String(rs.getBytes("grt_Nazwa"),"windows-1250");
                String id_kat=new String(""+rs.getInt("grt_Id"));
                System.out.println("KATEGORIA##"+test);
                out.write("KATEGORIA##"+test+"\n");  //zapis do pliku
                category = doc.createElement("kategoria");
                Attr attr = doc.createAttribute("id");
                attr.setValue(id_kat);
                category.setAttributeNode(attr);
                category.appendChild(doc.createTextNode(test));
                categoryElement.appendChild(category);

                if (!cetMap.containsKey(test)) {
                    cetMap.put(test,id_kat);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(folderXml+"kategorie.xml"));
            transformer.transform(source, result);
            //KONIEC KATEGORIE


            // CECHY select distinct ctw_Nazwa from sl_CechaTw
            HashMap<String, String> cechyMap = new HashMap<String, String>();
            HashMap<String, String> cechyMapStr = new HashMap<String, String>();
            HashMap<String, Element> cechyElMap = new HashMap<String, Element>();
            HashMap<String, String> cechyOpcjiMap = new HashMap<String, String>();

            HashSet<String> confs = new HashSet<String>();
            Document docFeature = docBuilder.newDocument();
            Element featureElement = docFeature.createElement("cechy");
            docFeature.appendChild(featureElement);
            Element feature = doc.createElement("cecha");
//select ctw_Id,ctw_Nazwa from sl_CechaTw
            queryString = "select ctw_Id,ctw_Nazwa from sl_CechaTw"; // where type='u'
            rs = statement.executeQuery(queryString);
            while (rs.next()) {
                //String test=rs.getString("ctw_Nazwa");
                String id_cechy=new String(""+rs.getString("ctw_Id"));
                String id_cechy_str="";
                String test=new String(rs.getBytes("ctw_Nazwa"),"windows-1250");
                String id_atrybutu=id_cechy;

                System.out.println("CECHA##"+test);
                out.write("CECHA##"+test+"\n");  //zapis do pliku

              //todo  warunek na rozmiat
              //  if (!test.contains("ppwe") && test.contains("rozmiar") ) {
              //      test="ppwe:"+test;
              //  }
                String s[] = test.split(":");



                if (s[0].equalsIgnoreCase("ppwe") || s[0].equalsIgnoreCase("conf") ) {

                /*    if (!cechyMap.containsKey(s[1])) {
                        cechyMap.put(s[1],id_cechy);
                    } else {
                        id_cechy = cechyMap.get(s[1]);
                    }*/
                    id_cechy_str=toPrettyURL(s[1]);
                    if (!cechyMap.containsKey(s[1])) {
                        cechyMap.put(s[1],id_cechy);
                        cechyMapStr.put(s[1],id_cechy_str);
                    } else {
                        id_cechy = cechyMap.get(s[1]);
                    }
                    if (s[0].equalsIgnoreCase("conf") && !confs.contains(id_cechy_str)) {
                        confs.add(id_cechy_str);
                    }
                    cechyOpcjiMap.put(id_cechy+"_"+s[2],id_atrybutu);

                    if (!cechyElMap.containsKey(id_cechy)) {
                        feature = docFeature.createElement("cecha");
                        cechyElMap.put(id_cechy, feature);
                    } else {
                        feature = cechyElMap.get(id_cechy);
                    }
                    Attr attr = docFeature.createAttribute("nazwa");
                    attr.setValue(s[1]);
                    feature.setAttributeNode(attr);

                    Attr attr2 = docFeature.createAttribute("id");
                   // attr2.setValue(id_cechy);
                    //ZMIANA
                    attr2.setValue(id_cechy_str);
                    feature.setAttributeNode(attr2);

                    Element atrybut = docFeature.createElement("atrybutcechy");
                    Attr attr3 = docFeature.createAttribute("id_opcji");
                    attr3.setValue(id_atrybutu);
                    atrybut.setAttributeNode(attr3);
                    atrybut.appendChild(docFeature.createTextNode(s[2]));
                    feature.appendChild(atrybut);

                  //  feature.appendChild(docFeature.createTextNode(s[2]));
                    featureElement.appendChild(feature);
                }
            }
            DOMSource sourceFeature = new DOMSource(docFeature);
            StreamResult resultFeature = new StreamResult(new File(folderXml+"cechy.xml"));
            transformer.transform(sourceFeature, resultFeature);
            //KONIEC CECHY


            // PRODUKTY Konfigurowalen
            Document docProducts = docBuilder.newDocument();
            Element productsElement = docProducts.createElement("produkty");
            docProducts.appendChild(productsElement);

            HashMap<String, String> konfigurowalne = new HashMap<String, String>();
            HashMap<String, String> konfigurowalne_cechy = new HashMap<String, String>();
            //   Element product = docProducts.createElement("produkt"); //utwórz produkt
           // Element prodElement = null; //utwórz produkt
           queryString = "select r.grt_Nazwa kategoria,t.tw_Id id, t.tw_Symbol symbol,t.tw_Nazwa nazwa, c.tc_CenaBrutto1 cena, c.tc_CenaBrutto4 cena2,t.tw_Charakter opis from dbo.tw__Towar t, dbo.tw_Cena c, dbo.sl_GrupaTw r where c.tc_Id=t.tw_Id and r.grt_Id=t.tw_IDGrupa and t.tw_SklepInternet=1";
          //  queryString = "select r.grt_Nazwa kategoria,t.tw_Id id, t.tw_Symbol symbol,t.tw_Nazwa nazwa, c.tc_CenaBrutto1 cena, c.tc_CenaBrutto4 cena2,'nic' opis from dbo.tw__Towar t, dbo.tw_Cena c, dbo.sl_GrupaTw r where c.tc_Id=t.tw_Id and r.grt_Id=t.tw_IDGrupa and t.tw_SklepInternet=1";
          /*  queryString = queryString+"and (t.tw_Nazwa like '%Enchant 1 granatowy 2015%' or t.tw_Nazwa like '%Propel Advanced 1 2015%'" +
                    "  or t.tw_Nazwa like '%Revel Street ciemnozielony 2015%'" +
                    "     or t.tw_Nazwa like '%Revel Street ciemnozielony 2015%'" +
                    "  or  t.tw_Nazwa like '%Revel Street jasnoniebieski 2015%')";*/
            // where type='u'
            queryString = queryString+" order by nazwa";
            rs = statement.executeQuery(queryString);
            int i=0;
            while (rs.next()) {
                if (i++ % 10 == 0) { System.out.println("Rekord nr:"+i); }
               Element product = docProducts.createElement("produkt");
                productsElement.appendChild(product);
                // String kat=rs.getString("kategoria");
                String kat=new String(rs.getBytes("kategoria"),"windows-1250");
                // String id=rs.getString("id");
                // String id=new String(rs.getBytes("id"),"windows-1250");
                Integer id=rs.getInt("id");
                // String sym=(String)rs.getString("symbol");
                String sym=new String(rs.getBytes("symbol"),"windows-1250");
                // String nazwa=(String)rs.getString("nazwa");
                String nazwa=new String(rs.getBytes("nazwa"),"windows-1250");
                Double cena=rs.getDouble("cena");
                Double cena2=rs.getDouble("cena2");
                //  String opis=""+rs.getString("opis");
                String opis="";
                byte[] tek=null;
                try {
                     tek = rs.getBytes("opis");
                }catch (SQLException sql) {

                }
                String nazwapliku=id+".htm";
                if (tek==null) {
                //    System.out.println("-------------------------");
                } else {
                    opis=""+new String(tek,"windows-1250");
                    String opisStr=opis;
                    opisStr=cleanTagPerservingLineBreaks(opis);
                    FileWriter fstreamOpis = new FileWriter(folder+nazwapliku);
                    BufferedWriter outOpis = new BufferedWriter(fstreamOpis);
                    outOpis.write(opisStr);
                    outOpis.close();
                }


                Integer stan=0;
//                Connection conn2 = DriverManager.getConnection(db_connect_string,db_userid,db_password);
   //             Statement statementSt = conn2.createStatement();
                Statement statementSt = conn.createStatement();
                String queryStanmag="select sum(st_Stan) stan from tw_Stan where st_TowId="+id;
                ResultSet rsStan =statementSt.executeQuery(queryStanmag);
                while (rsStan.next()) {
                    stan=rsStan.getInt("stan");
                }
  //              conn2.close();


                String linia="PRODUKT###"+kat+"###"+id+"###"+sym+"###"+nazwa+"###"+cena.doubleValue()+"###"+cena2.shortValue()+"###"+stan;

                Element prodElement = docProducts.createElement("kategoria");
                String katrnazwa=cetMap.get(kat);

                prodElement.appendChild(docProducts.createTextNode(katrnazwa));
                product.appendChild(prodElement);
                prodElement = docProducts.createElement("id");
                prodElement.appendChild(docProducts.createTextNode(id.toString()));
                product.appendChild(prodElement);
                prodElement = docProducts.createElement("sku");
                prodElement.appendChild(docProducts.createTextNode(sym));
                product.appendChild(prodElement);
                prodElement = docProducts.createElement("nazwa");
                prodElement.appendChild(docProducts.createTextNode(nazwa));
                product.appendChild(prodElement);
                prodElement = docProducts.createElement("cena");
                prodElement.appendChild(docProducts.createTextNode(cena.toString()));
                product.appendChild(prodElement);
                if (cena2.doubleValue() > 0) { //jesli nie ustawiona cenapromocyjna
                    prodElement = docProducts.createElement("promocja");
                    prodElement.appendChild(docProducts.createTextNode(cena2.toString()));
                    product.appendChild(prodElement);
                }
                prodElement = docProducts.createElement("stan");
                prodElement.appendChild(docProducts.createTextNode(stan.toString()));
                product.appendChild(prodElement);

                if (tek!=null) {
                    linia=linia+"###OPIS##"+nazwapliku;
                    prodElement = docProducts.createElement("opis");
                    prodElement.appendChild(docProducts.createTextNode(nazwapliku));
                    product.appendChild(prodElement);
                }
                //todo remove
           //     Connection conn3 = DriverManager.getConnection(db_connect_string,db_userid,db_password);
             //   Statement statementCechy = conn3.createStatement();
                Statement statementCechy = conn.createStatement();
                String queryCechy="select distinct ctw_Nazwa cecha from tw_CechaTw c,sl_CechaTw s where c.cht_IdCecha=s.ctw_Id and c.cht_IdTowar="+id;
                ResultSet rsCechy =statementCechy.executeQuery(queryCechy);

                Element cechy = docProducts.createElement("cechy");
                product.appendChild(cechy);
                String producent="";
                while (rsCechy.next()) {
                     String cechapom=rsCechy.getString("cecha");
                  //  String cechapom=new String(rsCechy.getString("cecha"),"windows-1250");
                    String cecha="###CECHA##"+cechapom;

                   //todo  warunek na rozmiat
                 //   if (!cechapom.contains("ppwe") && cechapom.contains("rozmiar") ) {
                 //       cechapom="ppwe:"+cechapom;
                 //   }
                    if (cechapom.contains("ppwe") || cechapom.contains("conf")  ) {

                        String s[] = cechapom.split(":");

                        Element cechaa = docProducts.createElement("cecha");
                        String id_cechy = cechyMap.get(s[1]);
                        String id_cechy_str= cechyMapStr.get(s[1]);

                        String id_atrybutu = cechyOpcjiMap.get(id_cechy + "_" + s[2]);
                        Attr attr2 = docProducts.createAttribute("id");
                        attr2.setValue(id_cechy_str);
                        cechaa.setAttributeNode(attr2);

                        cechaa.appendChild(docProducts.createTextNode(id_atrybutu));
                        cechy.appendChild(cechaa);

                       if (confs.contains(toPrettyURL(s[1]))) {
                           if (konfigurowalne_cechy.containsKey(nazwa)) {
                               if (!konfigurowalne_cechy.get(nazwa).contains(toPrettyURL(s[1]))) {
                                   konfigurowalne_cechy.put(nazwa, konfigurowalne_cechy.get(nazwa) + "," + toPrettyURL(s[1]));
                               }
                           } else {
                               konfigurowalne_cechy.put(nazwa, toPrettyURL(s[1]));
                           }

                           if (konfigurowalne.containsKey(nazwa)) {
                               konfigurowalne.put(nazwa, konfigurowalne.get(nazwa) + "," + sym);
                           } else {
                               konfigurowalne.put(nazwa,sym);
                           }

                       }

                        if (s[1].equalsIgnoreCase("producent")) {
                            producent = s[2];
                        }



      /*                  if (s[1].equalsIgnoreCase("konfigurowalny") && s[2].equalsIgnoreCase("tak")) {
                            if (konfigurowalne.containsKey(nazwa)) {
                                konfigurowalne.put(nazwa, konfigurowalne.get(nazwa) + "," + sym);
                            } else {
                                konfigurowalne.put(nazwa,sym);
                            }
                        }*/

                        linia = linia + cecha;
                    }
                }
            //    conn.close();
                //todo remove
              //  Connection conn4 = DriverManager.getConnection(db_connect_string,db_userid,db_password);
               // statementSt = conn4.createStatement();
                statementSt = conn.createStatement();
                String queryImg="select zd_Zdjecie from tw_ZdjecieTw where zd_IdTowar="+id;
                if (onlyStan==true){
                    queryImg="select zd_Id from tw_ZdjecieTw where zd_IdTowar="+id;
                }
                ResultSet rsStan3 =statementSt.executeQuery(queryImg);
                Element zdjecia = docProducts.createElement("zdjecia");
                product.appendChild(zdjecia);
                int idd=0;

               while (rsStan3.next()) {
                   try {
                       String nazwap = id + "_" + idd + ".jpg";
                       idd++;
                       //todo  zapisywanie obrazu
                       if (onlyStan ==false) {
                           byte[] fileBytes = rsStan3.getBytes(1);
                           OutputStream targetFile =
                                   new FileOutputStream(
                                           folder + nazwap);

                           targetFile.write(fileBytes);
                           targetFile.close();
                       }

                       linia = linia + "###IMAGE##" + nazwap;
                       Element zdjecie = docProducts.createElement("zdjecie");

                       Attr attr = docProducts.createAttribute("typ");
                       //                      if (glowne) {
                       //                          attr.setValue("base");
                       //                     }
                       zdjecie.setAttributeNode(attr);

                       Attr attr2 = docProducts.createAttribute("opis");
                       attr2.setValue(producent + " " + nazwa);
                       zdjecie.setAttributeNode(attr2);
                       zdjecie.appendChild(docProducts.createTextNode(nazwap));
                       zdjecia.appendChild(zdjecie);

                   }catch (Exception sql) {
                       System.out.println("err"+id);
                   }
                }
                //conn4.close();


              //  System.out.println(linia);
              //  out.write(linia+"\n");  //zapis do pliku


                //this.streamResult();
            }

            out.close();
            // write the content into xml file
            DOMSource sourcePR = new DOMSource(docProducts);
            StreamResult resultPr = new StreamResult(new File(folderXml+"produkty.xml"));
            transformer.transform(sourcePR, resultPr);


            Document konfProducts = docBuilder.newDocument();
            Element konfElement = konfProducts.createElement("konfigurowalne");
            konfProducts.appendChild(konfElement);
            for (String key : konfigurowalne.keySet()) {
                category = konfProducts.createElement("rodzina");
                Attr attr2 = konfProducts.createAttribute("cechy");
                //attr2.setValue("201");
                attr2.setValue(konfigurowalne_cechy.get(key));
                category.setAttributeNode(attr2);


                Attr attr = konfProducts.createAttribute("nazwa");
                attr.setValue(key);
                category.setAttributeNode(attr);
                category.appendChild(konfProducts.createTextNode(konfigurowalne.get(key)));
                konfElement.appendChild(category);
            }
            // write the content into xml file
            DOMSource sourcePR2 = new DOMSource(konfProducts);
            StreamResult resultPr2 = new StreamResult(new File(folderXml+"konfigurowalne.xml"));
            transformer.transform(sourcePR2, resultPr2);

            System.out.println("File saved!");

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


} 