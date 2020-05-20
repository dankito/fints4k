
## Konto hinzufügen

- Wie 'Konto hinzufügen' gut ersichtlich platzieren?

- UX des Konto hinzufügen Dialogs: Wie kann dieser einfach und intuitiv gestaltet werden?

- Daten, die ich brauche:
    - Bankleitzahl
    - Adresse des FinTS Servers -> über integrierte Bankenlisten anhand eingegebenener Bankleitzahl. Ermöglichen FinTS Serveradresse manuell einzugeben? Oder dieses Feld besser ganz verstecken?
    - "Kundennummer" = Nutzername mit dem mit sich beim Online Banking einloggt
    - Passwort (+ Option dieses in der App zu speichern?)

- Wie kann gut dargestellt werden, dass eine Bank FinTS nicht unterstützt und deshalb nicht in der App verwendet werden kann?

- Wie kann ein Fehler beim Hinzufügen eines Kontos, z. B. Passwort ist falsch, Konto existiert nicht oder Server kann nicht erreicht werden (Netzwerkfehler) gut dargestellt werden?

- Dialog nach erfolgreichem Hinzufügen eines Kontos: Dass die Bank das Abruf der Kontoumsätze der letzten 90 Tage ohne TAN Eingabe unterstützt oder nicht und ob jetzt alle Kontoumsätze abgerufen werden sollen, wofür die Eingabe einer TAN notwendig ist? - zu kompliziert?


## Anzeige Hauptbildschirm

- Toolbar überfrachtet mit Anzeige Hamburger Icon, Appname, Suchicon, Kontoumsätze aktualisieren Icon und Anzeige des Saldos?

- Eigener Bildschirm um die Salden der verschiedenen Konten in einer Übersicht anzuzeien?


## Anzeige der Kontoumsätze

- Wie kann klar gemacht werden, welcher Umsatz zu welchem Konto gehört (Hintergrundfarbe, Icon der Bank, ...)?

- Darstellung der Umsätze:
    - Welche Informationen zeige ich an?
    - Wie ordne ich diese Informationen auf dem knapp bemessenen Platz eines Handydisplays (5'' Display als Referenz) an?

- Beim App Start:
    - Umsätze aller Konten abfragen? -> Man muss bei manchen Banken eine TAN eingeben ohne dass der Nutzer erstmal sieht warum?
    - Nur die Umsätze von Konten abfragen, für die für die letzten 90 Tage keine TAN eingegeben werden muss? -> Evtl. inkonsistent, da manche Konten dann abgefragt werden und andere nicht ohne dass dies für den Nutzer offensichtlich ist.
    - Kein Konto automatisiert abfragen -> Der Nutzer muss selbst auf den Button klicken.
    
- Auch nicht gebuchte Umsätze anzeigen? (Wie von den Gebuchten abheben?)

- Wenn Kontoumsätze gefiltert sind (Suche): Nur die Summe der angezeigten Umsätze anzeigen oder weiterhin das Saldo der ausgewählten Konten?


## Auswahl der Konten

- Wie kann klar gemacht werden, wo ich zwischen den einzelnen Konten wechseln kann?

- Wie wird in der Kontoübersicht das ausgewähltes Konto dargestellt / hervorgehoben?

- Konto hinzufügen in der Navbar / Sidebar auch dann noch anzeigen wenn bereits ein Konto hinzugefügt wurde?

- Icon für 'Alle Konten'? Überhaupt Icons für Banken anzeigen?

- Icons für die unterschiedlichen Kontoarten, z. B. Girokonto, Kreditkartenkonto, Wertpapierkonto, Bausparkonto, ...?

- Dürfen favicons rechtlich gesehen in der App verwendet werden?

- Welche Informationen werden im Navigation Item für Banken / Kundenkonto bzw. Bankkonten angezeigt?


## Überweisungsdialog

- Wie findet der Nutzer den Überweisungsdialog?

- Wie zeige ich dem Nutzer Fehleingaben an?

- Dialog zu überfrachtet?

- Benötigte Informationen:
    - Empfängername
    - Empfänger-IBAN und -BIC (jedoch nicht Empfängerbankname, dient nur zur Information des Nutzers)
    - Betrag (Währungsauswahl?)
    - (Und ebenfalls _nicht_: Verwendungszweck. Ist rein optional)
    
- Verwendungszweckfeld zwei-/mehrzeilig?

- Darstellung zu verwendendes Konto (wird nur angezeigt falls mehr als ein Konto Überweisungen unterstützt): Icon? Anzuzeigender Text?

- Dem Nutzer die Eingabe der BIC erlauben? (Was ist, wenn ich die BIC nicht automatisiert ermitteln kann, z. B. bei Auslandsüberweisungen?)


## TAN-Eingabe Dialog

- Dialog zu überfrachtet?

- Anzeige des betreffenden Kontos (nur bei mehr als einem hinzugefügten Konto)? Wäre z. B. nützlich bei automatischem Kontorundruf bei App Start, damit der Nutzer weiß um welches Konto es sich handelt.

- Anzeige des zu verwendenden TAN Mediums (Chip Karte oder Handy)? Damit der Nutzer weiß, welches TAN Medium er verwenden soll (sollte aber in den meisten Fällen eh klar sein).
Ausnahme wo die Anzeige zwingend notwendig ist: Wenn ein Kartenwechsel ansteht, damit der Nutzer die richtige Karte wählen kann.

- Anzeige TAN Verfahren (nur falls mehr als eines zur Auswahl steht): Icons für die einzelnen TAN Verfahren (wären wohl nur: manuell, Flickercode, QR-Code, photoTAN/Matrix Code)?

- Darstellung Kontrollelemente für Größe und bei Flickercode Geschwindigkeit und Pause (Pause überhaupt anzeigen?)
