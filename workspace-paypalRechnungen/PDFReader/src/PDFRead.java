import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.DoubleStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.asprise.util.pdf.PDFReader;

public class PDFRead {

	public static String idfull = "";
	public static String id = "";
	public static String idString = "0";
	public static String datum, name, straße, ort, ebayname, artikelname;

	public static double versand, verkaufspreis, gesamtbetrag, gebuehr, sonstigeKostenGesamt, gewinn;

	public static WriteExcel we = new WriteExcel();

	public static double umsatzSumme, verkaufspreisSumme, versandSumme, paypalgebührenSumme;

	// Vars für Rückgabe
	public static List<String> rueckgabeList = new ArrayList<String>();
	public static String idRueckgabe = "";
	public static double kostenGesamtRueckgabe;
	public static double umsatzSummeRueckgabe, verkaufspreisSummeRueckgabe, VersandSummeRueckgabe, paypalgebührenSummeRueckgabe, zusaetzlicheVersandkostenSummeRueckgabe, gewinnNachRueckgabe;

	public static void listDir(File dir) throws IOException {

		File[] files = dir.listFiles();

		if (files != null) {

			for (int i = 0; i < files.length; i++) {
				// Auslesen der Id
				idRueckgabe = files[i].getName();
				idRueckgabe = idRueckgabe.substring(0, idRueckgabe.length() - 4);
				if (idRueckgabe.contains(".")) {
					idRueckgabe = idRueckgabe.substring(0, idRueckgabe.length() - 2);
					rueckgabeList.add(idRueckgabe);
				}
			}

			Map<String, Object[]> data = new TreeMap<String, Object[]>();
			we.setMapObject(data);
			we.getMapObject().put(idString, new Object[] { "Id", "Datum", "Name", "Straße", "Ort", "Ebayname",
					"Artikel", "Umsatz", "Verkaufspreis", "Versand", "Paypalgebühren" });

			for (int i = 0; i < files.length; i++) {
				// Auslesen der Id
				id = files[i].getName();
				id = id.substring(0, id.length() - 4);

				if (!id.contains(".")) {

					PDFReader reader = new PDFReader(new File(files[i].getAbsolutePath()));
					reader.open(); // open the file.
					int pages = reader.getNumberOfPages();

					for (int j = 0; j < pages; j++) {
						String text = reader.extractTextFromPage(j);

						StringBuffer sbFormattedText = new StringBuffer(text);

						// Auslesen von der Adresse
						getAdresse(text, sbFormattedText);

						// Auslesen vom Datum
						datum = getDatum(text, sbFormattedText);

						// Auslesen von Ebayname
						ebayname = getEbayname(text, sbFormattedText);

						// Auslesen vom Verkaufspreis
						verkaufspreis = getVerkaufspreis(text, sbFormattedText);
						verkaufspreisSumme += verkaufspreis;

						// Auslesen von Versand (wird atm hardcoded)
						versand = getVersand();
						versandSumme += versand;

						// Gesamtpreis durch berechnung NICHT AUSGELESEN
						gesamtbetrag = getGesamtbetrag(verkaufspreis, versand);
						umsatzSumme += gesamtbetrag;

						// Auslesen der Paypalgebuehr
						gebuehr = getPaypalgebuehr(gesamtbetrag);
						paypalgebührenSumme += gebuehr;

						// Auslesen des Artikelnamens
						artikelname = getArtikelname(text, sbFormattedText);

						// Sammeln der Rückgabekosten
						if (rueckgabeList.contains(id)) {
							verkaufspreisSummeRueckgabe+=verkaufspreis;
							VersandSummeRueckgabe+=versand;
							paypalgebührenSummeRueckgabe+=gebuehr;
							umsatzSummeRueckgabe+=gesamtbetrag;
							zusaetzlicheVersandkostenSummeRueckgabe+=4.99;
						}

						idString = id.toString();

						we.getMapObject().put(idString,
								new Object[] { idString, datum, name, straße, ort, ebayname, artikelname,
										String.valueOf(gesamtbetrag), String.valueOf(verkaufspreis),
										String.valueOf(versand), String.valueOf(gebuehr) });
					}

					// Gewinn aulesen 
					gewinn = getGewinn(umsatzSumme, versandSumme, paypalgebührenSumme, sonstigeKostenGesamt, 0);
					gewinnNachRueckgabe = getGewinn(umsatzSumme - umsatzSummeRueckgabe, versandSumme - VersandSummeRueckgabe, paypalgebührenSumme - paypalgebührenSummeRueckgabe, sonstigeKostenGesamt, zusaetzlicheVersandkostenSummeRueckgabe );

					// Einfügen der Zusammenfassungstitel
					we.getMapObject().put("Zusammenfassung Ohne Rückgabe Titel",
							new Object[] { "Zusammenfassung ohne Rückgabe:", "Umsatz Gesamt", "Verkaufspreis Gesamt",
									"Versandkosten Gesamt", "Paypalgebühren Gesamt", "Sonstige Kosten", 
									"Gewinn Gesamt" });
					
					// Einfügen der Zusammenfassungswerte
					we.getMapObject().put("Zusammenfassung Ohne Rückgabe Werte", new Object[] { " ",
							String.valueOf(round(umsatzSumme, 2)), String.valueOf(round(verkaufspreisSumme, 2)),
							String.valueOf(round(versandSumme, 2)), String.valueOf(round(paypalgebührenSumme, 2)),
							String.valueOf(round(sonstigeKostenGesamt, 2)), String.valueOf(round(gewinn, 2)) });
					
					// Einfügen der Zusammungsfassungstitel der Rückgabe
					we.getMapObject().put("Zusammenfassung Rückgabe Titel",
							new Object[] { "Zusammenfassung nach Rückgabe:", "(-) Umsatz Gesamt Rückgabe", "(-) Verkaufspreis Gesamt Rückgabe",
									"(-) Versandkosten Gesamt Rückgabe", "(-) Paypalgebühren Gesamt Rückgabe", "(+) Zusätzliche Versandkosten durch Rückgabe", 
									"Gewinn Final"});
					
					// Einfügen der Zusammenfassungswerte der Rückgabe
					we.getMapObject().put("Zusammenfassung Rückgabe Werte", new Object[] { " ",
							String.valueOf(round(umsatzSummeRueckgabe, 2)), String.valueOf(round(verkaufspreisSummeRueckgabe, 2)),
							String.valueOf(round(VersandSummeRueckgabe, 2)), String.valueOf(round(paypalgebührenSummeRueckgabe, 2)),
							String.valueOf(round(zusaetzlicheVersandkostenSummeRueckgabe, 2)), String.valueOf(round(gewinnNachRueckgabe, 2)) });

					reader.close(); // finally, close the file.

				} else {

				}
			}
		}
		System.out.println(rueckgabeList);
	}

	// Methoden um alle nötigen Daten auszulesen
	// ================================================================================================================================================================================================================

	// Gewinn auslesen
	public static double getGewinn(double umsatzSumme, double versandSumme, double paypalgebührenSumme,
			double sonstigeKostenGesamt, double rueckgabeKostenGesammt) {
		double gewinn = umsatzSumme - versandSumme - paypalgebührenSumme - sonstigeKostenGesamt
				- rueckgabeKostenGesammt;

		return gewinn;
	}

	// Adresse auslesen
	public static void getAdresse(String text, StringBuffer sbFormattedText) {
		String kundenname = "";

		List<String> kundendaten = new ArrayList<String>();

		int indexKundenname = 0;

		if (text.contains("Gesendet an")) {
			indexKundenname = text.indexOf("Gesendet an");
			indexKundenname += 13;

		} else {
			indexKundenname = text.indexOf("Lieferadresse");
			indexKundenname += 15;

		}

		int indexDeutschland = text.indexOf("Deutschland");

		for (int h = 0; h < sbFormattedText.length(); h++) {
			if (indexKundenname < indexDeutschland) {
				kundenname += sbFormattedText.charAt(indexKundenname);
				indexKundenname++;

				if (sbFormattedText.charAt(indexKundenname) == '\n') {
					kundendaten.add(kundenname);
					kundenname = "";
				}
			} else {
				if (!kundendaten.isEmpty()) {
					if (kundendaten.size() >= 4) {
						PDFRead.name = kundendaten.get(0);
						PDFRead.straße = kundendaten.get(1) + " " + kundendaten.get(2);
						PDFRead.ort = kundendaten.get(3);
					} else {
						PDFRead.name = kundendaten.get(0);
						PDFRead.straße = kundendaten.get(1);
						PDFRead.ort = kundendaten.get(2);
					}
				}
				System.out.println(kundendaten);
				break;
			}
		}
	}

	// Datum auslesen
	public static String getDatum(String text, StringBuffer sbFormattedText) {
		String datum = "";
		int indexDatum = text.indexOf("Summe");
		indexDatum += 17;

		int indexDatumEnde = 0;

		if (text.contains("Sie erhalten")) {
			indexDatum = text.indexOf("Sie erhalten");
			indexDatum += 24;

			for (int h = 0; h < sbFormattedText.length(); h++) {
				if (sbFormattedText.charAt(indexDatum) == '\n') {
					System.out.println(datum);
					return datum;
				} else {
					datum += sbFormattedText.charAt(indexDatum);
					indexDatum++;
				}
			}

		} else {
			for (int h = 0; h < sbFormattedText.length(); h++) {
				if (sbFormattedText.charAt(indexDatum) == '\n') {
					System.out.println(datum);
					return datum;
				} else {
					datum += sbFormattedText.charAt(indexDatum);
					indexDatum++;
				}
			}
		}
		return "";
	}

	// Paypalgebühr auslesen
	public static double getPaypalgebuehr(double gesamtbetrag) {
		double gebuehrDouble = round(gesamtbetrag * 0.0249 + 0.35, 2); // formel für den prozentsatz für die
																		// paypalgebühren
		return gebuehrDouble;
	}

	// Gesamtbetrag auslesen
	public static double getGesamtbetrag(double verkaufspreis, double versand) {
		double gesamtbetragDouble = verkaufspreis + versand;

		return gesamtbetragDouble;
	}

	// Versand auslesen
	public static double getVersand() {
		double versand = 4.99;

		return versand;
	}

	// Verkaufspreis auslesen
	public static double getVerkaufspreis(String text, StringBuffer sbFormattedText) {
		String verkaufspreis = "";
		int indexVerkaufspreisEnde = text.indexOf("Artikelnr");
		indexVerkaufspreisEnde -= 6;
		int indexVerkaufspreisAnfang = indexVerkaufspreisEnde - 6;

		for (int h = 0; h < sbFormattedText.length(); h++) {
			if (indexVerkaufspreisAnfang < indexVerkaufspreisEnde) {
				verkaufspreis += sbFormattedText.charAt(indexVerkaufspreisAnfang);
				indexVerkaufspreisAnfang++;
			} else {
				verkaufspreis = verkaufspreis.replace(',', '.');
				System.out.println(verkaufspreis);
				double verkaufspreisDouble = Double.parseDouble(verkaufspreis);

				return verkaufspreisDouble;
			}
		}
		return -1;
	}

	// Ebayname auslesen
	public static String getEbayname(String text, StringBuffer sbFormattedText) {
		String ebayname = "";
		int indexEbaynameKlammerAuf = text.indexOf("(");
		indexEbaynameKlammerAuf += 1;
		int indexEbaynameKlammerZu = text.indexOf(")");

		for (int h = 0; h < sbFormattedText.length(); h++) {
			if (indexEbaynameKlammerAuf < indexEbaynameKlammerZu) {
				ebayname += sbFormattedText.charAt(indexEbaynameKlammerAuf);
				indexEbaynameKlammerAuf++;
			} else {
				System.out.println(ebayname);
				return ebayname;
			}
		}
		return "";
	}

	// Artikelname auslesen
	public static String getArtikelname(String text, StringBuffer sbFormattedText) {
		String artikelname = "";
		int indexArtikelnameAnfang = text.indexOf("Kaufdetails");
		indexArtikelnameAnfang += 11;
		int indexArtikelnameEnde = indexArtikelnameAnfang + 40;

		for (int h = 0; h < sbFormattedText.length(); h++) {
			if (indexArtikelnameAnfang > indexArtikelnameEnde) {
				System.out.println(artikelname);
				return artikelname;
			} else {
				artikelname += sbFormattedText.charAt(indexArtikelnameAnfang);
				indexArtikelnameAnfang++;
			}
		}
		return "";
	}

	// Hilfsmethoden
	// ================================================================================================================================================================================================================

	public static double getSum(List<Double> doubleListe) {
		double summe = 0;

		for (double element : doubleListe) {
			summe += element;
		}
		return summe;
	}

	public static boolean isNumeric(String strNum) {
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	public static double round(double value, int decimalPoints) {
		double d = Math.pow(10, decimalPoints);
		return Math.round(value * d) / d;
	}

	public static void main(String[] args) {

		try {
			ExcelReader.main();
		} catch (InvalidFormatException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			listDir(new File("C:\\Users\\nikita\\Desktop\\Ebay-Rechnungen - Kopie\\2019_Paypalbelege"));

			//listDir(new File("D:\\Workspaces\\workspace-paypalRechnungen\\PDFReader\\src"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		we.putDataObject();

	}

}
