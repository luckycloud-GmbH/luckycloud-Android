# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

## Main toolbar buttons (tooltips and alt text for images)

pdfjs-previous-button =
    .title = Vorherige Seite
pdfjs-previous-button-label = Vorherige
pdfjs-next-button =
    .title = Nächste Seite
pdfjs-next-button-label = Nächste

# .title: Tooltip for the pageNumber input.
pdfjs-page-input =
    .title = Seite

# Variables:
#   $pagesCount (Number) - the total number of pages in the document
# This string follows an input field with the number of the page currently displayed.
pdfjs-of-pages = von { $pagesCount }

# Variables:
#   $pageNumber (Number) - the currently visible page
#   $pagesCount (Number) - the total number of pages in the document
pdfjs-page-of-pages = ({ $pageNumber } von { $pagesCount })

pdfjs-zoom-out-button =
    .title = Verkleinern
pdfjs-zoom-out-button-label = Verkleinern
pdfjs-zoom-in-button =
    .title = Vergrößern
pdfjs-zoom-in-button-label = Vergrößern
pdfjs-zoom-select =
    .title = Zoom
pdfjs-presentation-mode-button =
    .title = In Präsentationsmodus wechseln
pdfjs-presentation-mode-button-label = Präsentationsmodus
pdfjs-open-file-button =
    .title = Datei öffnen
pdfjs-open-file-button-label = Öffnen
pdfjs-print-button =
    .title = Drucken
pdfjs-print-button-label = Drucken
pdfjs-save-button =
    .title = Speichern
pdfjs-save-button-label = Speichern

# Used in Firefox for Android as a tooltip for the download button ("download" is a verb).
pdfjs-download-button =
    .title = Herunterladen

# Used in Firefox for Android as a label for the download button ("download" is a verb).
# Length of the translation matters since we are in a mobile context, with limited screen estate.
pdfjs-download-button-label = Herunterladen

pdfjs-bookmark-button =
    .title = Aktuelle Seite (URL von aktueller Seite anzeigen)
pdfjs-bookmark-button-label = Aktuelle Seite

##  Secondary toolbar and context menu

pdfjs-tools-button =
    .title = Werkzeuge

pdfjs-tools-button-label = Werkzeuge
pdfjs-first-page-button =
    .title = Zur ersten Seite gehen
pdfjs-first-page-button-label = Zur ersten Seite gehen
pdfjs-last-page-button =
    .title = Zur letzten Seite gehen
pdfjs-last-page-button-label = Zur letzten Seite gehen
pdfjs-page-rotate-cw-button =
    .title = Im Uhrzeigersinn drehen
pdfjs-page-rotate-cw-button-label = Im Uhrzeigersinn drehen
pdfjs-page-rotate-ccw-button =
    .title = Gegen den Uhrzeigersinn drehen
pdfjs-page-rotate-ccw-button-label = Gegen den Uhrzeigersinn drehen
pdfjs-cursor-text-select-tool-button =
    .title = Textauswahl-Werkzeug aktivieren
pdfjs-cursor-text-select-tool-button-label = Textauswahl-Werkzeug
pdfjs-cursor-hand-tool-button =
    .title = Hand-Werkzeug aktivieren
pdfjs-cursor-hand-tool-button-label = Hand-Werkzeug
pdfjs-scroll-page-button =
    .title = Seitenscrollen verwenden
pdfjs-scroll-page-button-label = Seitenscrollen
pdfjs-scroll-vertical-button =
    .title = Vertikales Scrollen verwenden
pdfjs-scroll-vertical-button-label = Vertikales Scrollen
pdfjs-scroll-horizontal-button =
    .title = Horizontales Scrollen verwenden
pdfjs-scroll-horizontal-button-label = Horizontales Scrollen
pdfjs-scroll-wrapped-button =
    .title = Umbrochenes Scrollen verwenden
pdfjs-scroll-wrapped-button-label = Umbrochenes Scrollen
pdfjs-spread-none-button =
    .title = Keine Doppelseiten verbinden
pdfjs-spread-none-button-label = Keine Doppelseiten
pdfjs-spread-odd-button =
    .title = Doppelseiten beginnend mit ungeraden Seitenzahlen verbinden
pdfjs-spread-odd-button-label = Ungerade Doppelseiten
pdfjs-spread-even-button =
    .title = Doppelseiten beginnend mit geraden Seitenzahlen verbinden
pdfjs-spread-even-button-label = Gerade Doppelseiten

## Document properties dialog

pdfjs-document-properties-button =
    .title = Dokumenteigenschaften…
pdfjs-document-properties-button-label = Dokumenteigenschaften…
pdfjs-document-properties-file-name = Dateiname:
pdfjs-document-properties-file-size = Dateigröße:

# Variables:
#   $kb (Number) - the PDF file size in kilobytes
#   $b (Number) - the PDF file size in bytes
pdfjs-document-properties-size-kb = { NUMBER($kb, maximumSignificantDigits: 3) } KB ({ $b } Bytes)

# Variables:
#   $mb (Number) - the PDF file size in megabytes
#   $b (Number) - the PDF file size in bytes
pdfjs-document-properties-size-mb = { NUMBER($mb, maximumSignificantDigits: 3) } MB ({ $b } Bytes)

pdfjs-document-properties-title = Titel:
pdfjs-document-properties-author = Autor:
pdfjs-document-properties-subject = Betreff:
pdfjs-document-properties-keywords = Schlüsselwörter:
pdfjs-document-properties-creation-date = Erstellungsdatum:
pdfjs-document-properties-modification-date = Änderungsdatum:

# Variables:
#   $dateObj (Date) - the creation/modification date and time of the PDF file
pdfjs-document-properties-date-time-string = { DATETIME($dateObj, dateStyle: "short", timeStyle: "medium") }

pdfjs-document-properties-creator = Ersteller:
pdfjs-document-properties-producer = PDF-Ersteller:
pdfjs-document-properties-version = PDF-Version:
pdfjs-document-properties-page-count = Seitenzahl:
pdfjs-document-properties-page-size = Seitengröße:
pdfjs-document-properties-page-size-unit-inches = Zoll
pdfjs-document-properties-page-size-unit-millimeters = mm
pdfjs-document-properties-page-size-orientation-portrait = Hochformat
pdfjs-document-properties-page-size-orientation-landscape = Querformat
pdfjs-document-properties-page-size-name-a-three = A3
pdfjs-document-properties-page-size-name-a-four = A4
pdfjs-document-properties-page-size-name-letter = Letter
pdfjs-document-properties-page-size-name-legal = Legal

## Variables:
##   $width (Number) - the width of the (current) page
##   $height (Number) - the height of the (current) page
##   $unit (String) - the unit of measurement of the (current) page
##   $name (String) - the name of the (current) page
##   $orientation (String) - the orientation of the (current) page

pdfjs-document-properties-page-size-dimension-string = { $width } × { $height } { $unit } ({ $orientation })
pdfjs-document-properties-page-size-dimension-name-string = { $width } × { $height } { $unit } ({ $name }, { $orientation })

##

# The linearization status of the document; usually called "Fast Web View" in
# English locales of Adobe software.
pdfjs-document-properties-linearized = Schnelle Webansicht:
pdfjs-document-properties-linearized-yes = Ja
pdfjs-document-properties-linearized-no = Nein
pdfjs-document-properties-close-button = Schließen

## Print

pdfjs-print-progress-message = Dokument wird für den Druck vorbereitet…

# Variables:
#   $progress (Number) - percent value
pdfjs-print-progress-percent = { $progress }%

pdfjs-print-progress-close-button = Abbrechen
pdfjs-printing-not-supported = Warnung: Drucken wird von diesem Browser nicht vollständig unterstützt.
pdfjs-printing-not-ready = Warnung: Die PDF-Datei ist nicht vollständig zum Drucken geladen.

## Tooltips and alt text for side panel toolbar buttons

pdfjs-toggle-sidebar-button =
    .title = Seitenleiste ein-/ausblenden
pdfjs-toggle-sidebar-notification-button =
    .title = Seitenleiste ein-/ausblenden (Dokument enthält Gliederung/Anhänge/Ebenen)
pdfjs-toggle-sidebar-button-label = Seitenleiste ein-/ausblenden
pdfjs-document-outline-button =
    .title = Dokumentgliederung anzeigen (Doppelklick zum Auf-/Zuklappen aller Einträge)
pdfjs-document-outline-button-label = Dokumentgliederung
pdfjs-attachments-button =
    .title = Anhänge anzeigen
pdfjs-attachments-button-label = Anhänge
pdfjs-layers-button =
    .title = Ebenen anzeigen (Doppelklick zum Zurücksetzen aller Ebenen auf den Standardzustand)
pdfjs-layers-button-label = Ebenen
pdfjs-thumbs-button =
    .title = Vorschaubilder anzeigen
pdfjs-thumbs-button-label = Vorschaubilder
pdfjs-current-outline-item-button =
    .title = Aktuellen Gliederungseintrag finden
pdfjs-current-outline-item-button-label = Aktueller Gliederungseintrag
pdfjs-findbar-button =
    .title = Im Dokument suchen
pdfjs-findbar-button-label = Suchen
pdfjs-additional-layers = Zusätzliche Ebenen

## Thumbnails panel item (tooltip and alt text for images)

# Variables:
#   $page (Number) - the page number
pdfjs-thumb-page-title =
    .title = Seite { $page }

# Variables:
#   $page (Number) - the page number
pdfjs-thumb-page-canvas =
    .aria-label = Vorschaubild von Seite { $page }

## Find panel button title and messages

pdfjs-find-input =
    .title = Suchen
    .placeholder = Im Dokument suchen…
pdfjs-find-previous-button =
    .title = Vorheriges Vorkommen des Begriffs finden
pdfjs-find-previous-button-label = Vorherige
pdfjs-find-next-button =
    .title = Nächstes Vorkommen des Begriffs finden
pdfjs-find-next-button-label = Nächste
pdfjs-find-highlight-checkbox = Alle hervorheben
pdfjs-find-match-case-checkbox-label = Groß-/Kleinschreibung beachten
pdfjs-find-match-diacritics-checkbox-label = Diakritische Zeichen beachten
pdfjs-find-entire-word-checkbox-label = Ganze Wörter
pdfjs-find-reached-top = Anfang des Dokuments erreicht, von unten fortgesetzt
pdfjs-find-reached-bottom = Ende des Dokuments erreicht, von oben fortgesetzt

# Variables:
#   $current (Number) - the index of the currently active find result
#   $total (Number) - the total number of matches in the document
pdfjs-find-match-count =
    { $total ->
        [one] { $current } von { $total } Treffer
       *[other] { $current } von { $total } Treffer
    }

# Variables:
#   $limit (Number) - the maximum number of matches
pdfjs-find-match-count-limit =
    { $limit ->
        [one] Mehr als { $limit } Treffer
       *[other] Mehr als { $limit } Treffer
    }

pdfjs-find-not-found = Begriff nicht gefunden

## Predefined zoom values

pdfjs-page-scale-width = Seitenbreite
pdfjs-page-scale-fit = Ganze Seite
pdfjs-page-scale-auto = Automatischer Zoom
pdfjs-page-scale-actual = Originalgröße

# Variables:
#   $scale (Number) - percent value for page scale
pdfjs-page-scale-percent = { $scale }%

## PDF page

# Variables:
#   $page (Number) - the page number
pdfjs-page-landmark =
    .aria-label = Seite { $page }

## Loading indicator messages

pdfjs-loading-error = Beim Laden der PDF-Datei ist ein Fehler aufgetreten.
pdfjs-invalid-file-error = Ungültige oder beschädigte PDF-Datei.
pdfjs-missing-file-error = Fehlende PDF-Datei.
pdfjs-unexpected-response-error = Unerwartete Serverantwort.
pdfjs-rendering-error = Beim Rendern der Seite ist ein Fehler aufgetreten.

## Annotations

# Variables:
#   $dateObj (Date) - the modification date and time of the annotation
pdfjs-annotation-date-time-string = { DATETIME($dateObj, dateStyle: "short", timeStyle: "medium") }

# .alt: This is used as a tooltip.
# Variables:
#   $type (String) - an annotation type from a list defined in the PDF spec
# (32000-1:2008 Table 169 – Annotation types).
# Some common types are e.g.: "Check", "Text", "Comment", "Note"
pdfjs-text-annotation-type =
    .alt = [{ $type } Anmerkung]

## Password

pdfjs-password-label = Geben Sie das Passwort ein, um diese PDF-Datei zu öffnen.
pdfjs-password-invalid = Ungültiges Passwort. Bitte versuchen Sie es erneut.
pdfjs-password-ok-button = OK
pdfjs-password-cancel-button = Abbrechen
pdfjs-web-fonts-disabled = Web-Schriftarten sind deaktiviert: Eingebettete PDF-Schriftarten können nicht verwendet werden.

## Editing

pdfjs-editor-free-text-button =
    .title = Text
pdfjs-editor-free-text-button-label = Text
pdfjs-editor-ink-button =
    .title = Zeichnen
pdfjs-editor-ink-button-label = Zeichnen
pdfjs-editor-stamp-button =
    .title = Bilder hinzufügen oder bearbeiten
pdfjs-editor-stamp-button-label = Bilder hinzufügen oder bearbeiten
pdfjs-editor-highlight-button =
    .title = Hervorheben
pdfjs-editor-highlight-button-label = Hervorheben
pdfjs-highlight-floating-button1 =
    .title = Hervorheben
    .aria-label = Hervorheben
pdfjs-highlight-floating-button-label = Hervorheben
pdfjs-editor-signature-button =
    .title = Signatur hinzufügen
pdfjs-editor-signature-button-label = Signatur hinzufügen

## Default editor aria labels

# "Highlight" is a noun, the string is used on the editor for highlights.
pdfjs-editor-highlight-editor =
    .aria-label = Hervorhebungseditor
# "Drawing" is a noun, the string is used on the editor for drawings.
pdfjs-editor-ink-editor =
    .aria-label = Zeichnungseditor

# Used when a signature editor is selected/hovered.
# Variables:
#   $description (String) - a string describing/labeling the signature.
pdfjs-editor-signature-editor1 =
    .aria-description = Signatur-Editor: { $description }

pdfjs-editor-stamp-editor =
    .aria-label = Bildeditor

## Remove button for the various kind of editor.

pdfjs-editor-remove-ink-button =
    .title = Zeichnung entfernen
pdfjs-editor-remove-freetext-button =
    .title = Text entfernen
pdfjs-editor-remove-stamp-button =
    .title = Bild entfernen
pdfjs-editor-remove-highlight-button =
    .title = Hervorhebung entfernen
pdfjs-editor-remove-signature-button =
    .title = Signatur entfernen

##

# Editor Parameters
pdfjs-editor-free-text-color-input = Farbe
pdfjs-editor-free-text-size-input = Größe
pdfjs-editor-ink-color-input = Farbe
pdfjs-editor-ink-thickness-input = Dicke
pdfjs-editor-ink-opacity-input = Deckkraft
pdfjs-editor-stamp-add-image-button =
    .title = Bild hinzufügen
pdfjs-editor-stamp-add-image-button-label = Bild hinzufügen
# This refers to the thickness of the line used for free highlighting (not bound to text)
pdfjs-editor-free-highlight-thickness-input = Dicke
pdfjs-editor-free-highlight-thickness-title =
    .title = Dicke beim Hervorheben von anderen Elementen als Text ändern
pdfjs-editor-add-signature-container =
    .aria-label = Signatur-Bedienelemente und gespeicherte Signaturen
pdfjs-editor-signature-add-signature-button =
    .title = Neue Signatur hinzufügen
pdfjs-editor-signature-add-signature-button-label = Neue Signatur hinzufügen

# Used on the button to use an already saved signature.
# Variables:
#   $description (String) - a string describing/labeling the signature.
pdfjs-editor-add-saved-signature-button =
    .title = Gespeicherte Signatur: { $description }

# .default-content is used as a placeholder in an empty text editor.
pdfjs-free-text2 =
    .aria-label = Texteditor
    .default-content = Tippen Sie hier…

## Alt-text dialog

# Alternative text (alt text) helps when people can't see the image.
pdfjs-editor-alt-text-button =
    .aria-label = Alt-Text
pdfjs-editor-alt-text-button-label = Alt-Text

pdfjs-editor-alt-text-edit-button =
    .aria-label = Alt-Text bearbeiten
pdfjs-editor-alt-text-dialog-label = Option wählen
pdfjs-editor-alt-text-dialog-description = Alt-Text (Alternativtext) hilft, wenn Personen das Bild nicht sehen können oder wenn es nicht geladen wird.
pdfjs-editor-alt-text-add-description-label = Beschreibung hinzufügen
pdfjs-editor-alt-text-add-description-description = Ziel sind 1-2 Sätze, die das Motiv, die Umgebung oder die Handlungen beschreiben.
pdfjs-editor-alt-text-mark-decorative-label = Als dekorativ markieren
pdfjs-editor-alt-text-mark-decorative-description = Dies wird für ornamentale Bilder verwendet, wie Rahmen oder Wasserzeichen.
pdfjs-editor-alt-text-cancel-button = Abbrechen
pdfjs-editor-alt-text-save-button = Speichern
pdfjs-editor-alt-text-decorative-tooltip = Als dekorativ markiert

# .placeholder: This is a placeholder for the alt text input area
pdfjs-editor-alt-text-textarea =
    .placeholder = Zum Beispiel: „Ein junger Mann setzt sich an einen Tisch, um eine Mahlzeit zu essen"

## Editor resizers
## This is used in an aria label to help to understand the role of the resizer.

pdfjs-editor-resizer-top-left =
    .aria-label = Obere linke Ecke — Größe ändern
pdfjs-editor-resizer-top-middle =
    .aria-label = Oben Mitte — Größe ändern
pdfjs-editor-resizer-top-right =
    .aria-label = Obere rechte Ecke — Größe ändern
pdfjs-editor-resizer-middle-right =
    .aria-label = Mitte rechts — Größe ändern
pdfjs-editor-resizer-bottom-right =
    .aria-label = Untere rechte Ecke — Größe ändern
pdfjs-editor-resizer-bottom-middle =
    .aria-label = Unten Mitte — Größe ändern
pdfjs-editor-resizer-bottom-left =
    .aria-label = Untere linke Ecke — Größe ändern
pdfjs-editor-resizer-middle-left =
    .aria-label = Mitte links — Größe ändern

## Color picker

# This means "Color used to highlight text"
pdfjs-editor-highlight-colorpicker-label = Hervorhebungsfarbe

pdfjs-editor-colorpicker-button =
    .title = Farbe ändern
pdfjs-editor-colorpicker-dropdown =
    .aria-label = Farbauswahl
pdfjs-editor-colorpicker-yellow =
    .title = Gelb
pdfjs-editor-colorpicker-green =
    .title = Grün
pdfjs-editor-colorpicker-blue =
    .title = Blau
pdfjs-editor-colorpicker-pink =
    .title = Rosa
pdfjs-editor-colorpicker-red =
    .title = Rot

## Show all highlights
## This is a toggle button to show/hide all the highlights.

pdfjs-editor-highlight-show-all-button-label = Alle anzeigen
pdfjs-editor-highlight-show-all-button =
    .title = Alle anzeigen

## New alt-text dialog
## Group note for entire feature: Alternative text (alt text) helps when people can't see the image. This feature includes a tool to create alt text automatically using an AI model that works locally on the user's device to preserve privacy.

# Modal header positioned above a text box where users can edit the alt text.
pdfjs-editor-new-alt-text-dialog-edit-label = Alt-Text bearbeiten (Bildbeschreibung)

# Modal header positioned above a text box where users can add the alt text.
pdfjs-editor-new-alt-text-dialog-add-label = Alt-Text hinzufügen (Bildbeschreibung)

pdfjs-editor-new-alt-text-textarea =
    .placeholder = Schreiben Sie hier Ihre Beschreibung…

# This text refers to the alt text box above this description. It offers a definition of alt text.
pdfjs-editor-new-alt-text-description = Kurze Beschreibung für Personen, die das Bild nicht sehen können oder wenn das Bild nicht geladen wird.

# This is a required legal disclaimer that refers to the automatically created text inside the alt text box above this text. It disappears if the text is edited by a human.
pdfjs-editor-new-alt-text-disclaimer1 = Dieser Alt-Text wurde automatisch erstellt und könnte ungenau sein.
pdfjs-editor-new-alt-text-disclaimer-learn-more-url = Mehr erfahren

pdfjs-editor-new-alt-text-create-automatically-button-label = Alt-Text automatisch erstellen
pdfjs-editor-new-alt-text-not-now-button = Nicht jetzt
pdfjs-editor-new-alt-text-error-title = Alt-Text konnte nicht automatisch erstellt werden
pdfjs-editor-new-alt-text-error-description = Bitte schreiben Sie Ihren eigenen Alt-Text oder versuchen Sie es später erneut.
pdfjs-editor-new-alt-text-error-close-button = Schließen

# Variables:
#   $totalSize (Number) - the total size (in MB) of the AI model.
#   $downloadedSize (Number) - the downloaded size (in MB) of the AI model.
pdfjs-editor-new-alt-text-ai-model-downloading-progress = Alt-Text-KI-Modell wird heruntergeladen ({ $downloadedSize } von { $totalSize } MB)
    .aria-valuetext = Alt-Text-KI-Modell wird heruntergeladen ({ $downloadedSize } von { $totalSize } MB)

# This is a button that users can click to edit the alt text they have already added.
pdfjs-editor-new-alt-text-added-button =
    .aria-label = Alt-Text hinzugefügt
pdfjs-editor-new-alt-text-added-button-label = Alt-Text hinzugefügt

# This is a button that users can click to open the alt text editor and add alt text when it is not present.
pdfjs-editor-new-alt-text-missing-button =
    .aria-label = Alt-Text fehlt
pdfjs-editor-new-alt-text-missing-button-label = Alt-Text fehlt

# This is a button that opens up the alt text modal where users should review the alt text that was automatically generated.
pdfjs-editor-new-alt-text-to-review-button =
    .aria-label = Alt-Text überprüfen
pdfjs-editor-new-alt-text-to-review-button-label = Alt-Text überprüfen

# "Created automatically" is a prefix that will be added to the beginning of any alt text that has been automatically generated. After the colon, the user will see/hear the actual alt text description. If the alt text has been edited by a human, this prefix will not appear.
# Variables:
#   $generatedAltText (String) - the generated alt-text.
pdfjs-editor-new-alt-text-generated-alt-text-with-disclaimer = Automatisch erstellt: { $generatedAltText }

## Image alt-text settings

pdfjs-image-alt-text-settings-button =
    .title = Bild Alt-Text-Einstellungen
pdfjs-image-alt-text-settings-button-label = Bild Alt-Text-Einstellungen

pdfjs-editor-alt-text-settings-dialog-label = Bild Alt-Text-Einstellungen
pdfjs-editor-alt-text-settings-automatic-title = Automatischer Alt-Text
pdfjs-editor-alt-text-settings-create-model-button-label = Alt-Text automatisch erstellen
pdfjs-editor-alt-text-settings-create-model-description = Schlägt Beschreibungen vor, um Personen zu helfen, die das Bild nicht sehen können oder wenn das Bild nicht geladen wird.

# Variables:
#   $totalSize (Number) - the total size (in MB) of the AI model.
pdfjs-editor-alt-text-settings-download-model-label = Alt-Text-KI-Modell ({ $totalSize } MB)

pdfjs-editor-alt-text-settings-ai-model-description = Läuft lokal auf Ihrem Gerät, sodass Ihre Daten privat bleiben. Erforderlich für automatischen Alt-Text.
pdfjs-editor-alt-text-settings-delete-model-button = Löschen
pdfjs-editor-alt-text-settings-download-model-button = Herunterladen
pdfjs-editor-alt-text-settings-downloading-model-button = Wird heruntergeladen…

pdfjs-editor-alt-text-settings-editor-title = Alt-Text-Editor
pdfjs-editor-alt-text-settings-show-dialog-button-label = Alt-Text-Editor sofort beim Hinzufügen eines Bildes anzeigen
pdfjs-editor-alt-text-settings-show-dialog-description = Hilft Ihnen dabei, sicherzustellen, dass alle Ihre Bilder Alt-Text haben.
pdfjs-editor-alt-text-settings-close-button = Schließen

## "Annotations removed" bar

pdfjs-editor-undo-bar-message-highlight = Hervorhebung entfernt
pdfjs-editor-undo-bar-message-freetext = Text entfernt
pdfjs-editor-undo-bar-message-ink = Zeichnung entfernt
pdfjs-editor-undo-bar-message-stamp = Bild entfernt
pdfjs-editor-undo-bar-message-signature = Signatur entfernt
# Variables:
#   $count (Number) - the number of removed annotations.
pdfjs-editor-undo-bar-message-multiple =
    { $count ->
        [one] { $count } Anmerkung entfernt
       *[other] { $count } Anmerkungen entfernt
    }

pdfjs-editor-undo-bar-undo-button =
    .title = Rückgängig
pdfjs-editor-undo-bar-undo-button-label = Rückgängig
pdfjs-editor-undo-bar-close-button =
    .title = Schließen
pdfjs-editor-undo-bar-close-button-label = Schließen

## Add a signature dialog

pdfjs-editor-add-signature-dialog-label = Dieses Modal ermöglicht es dem Benutzer, eine Signatur zu erstellen, um sie einem PDF-Dokument hinzuzufügen. Der Benutzer kann den Namen bearbeiten (der auch als Alt-Text dient) und optional die Signatur zur wiederholten Verwendung speichern.
pdfjs-editor-add-signature-dialog-title = Signatur hinzufügen

## Tab names

# Type is a verb (you can type your name as signature)
pdfjs-editor-add-signature-type-button = Eingeben
    .title = Eingeben
# Draw is a verb (you can draw your signature)
pdfjs-editor-add-signature-draw-button = Zeichnen
    .title = Zeichnen
pdfjs-editor-add-signature-image-button = Bild
    .title = Bild

## Tab panels

pdfjs-editor-add-signature-type-input =
    .aria-label = Geben Sie Ihre Signatur ein
    .placeholder = Geben Sie Ihre Signatur ein
pdfjs-editor-add-signature-draw-placeholder = Zeichnen Sie Ihre Signatur
pdfjs-editor-add-signature-draw-thickness-range-label = Dicke

# Variables:
#   $thickness (Number) - the thickness (in pixels) of the line used to draw a signature.
pdfjs-editor-add-signature-draw-thickness-range =
    .title = Zeichnungsdicke: { $thickness }

pdfjs-editor-add-signature-image-placeholder = Ziehen Sie eine Datei hier hin, um sie hochzuladen
pdfjs-editor-add-signature-image-browse-link =
    { PLATFORM() ->
        [macos] Oder Bilddateien auswählen
       *[other] Oder Bilddateien durchsuchen
    }

## Controls

pdfjs-editor-add-signature-description-label = Beschreibung (Alt-Text)
pdfjs-editor-add-signature-description-input =
    .title = Beschreibung (Alt-Text)
pdfjs-editor-add-signature-description-default-when-drawing = Signatur


pdfjs-editor-add-signature-clear-button-label = Signatur löschen
pdfjs-editor-add-signature-clear-button =
    .title = Signatur löschen
pdfjs-editor-add-signature-save-checkbox = Signatur speichern
pdfjs-editor-add-signature-save-warning-message = Sie haben das Limit von 5 gespeicherten Signaturen erreicht. Entfernen Sie eine, um weitere zu speichern.
pdfjs-editor-add-signature-image-upload-error-title = Bild konnte nicht hochgeladen werden
pdfjs-editor-add-signature-image-upload-error-description = Überprüfen Sie Ihre Netzwerkverbindung oder versuchen Sie ein anderes Bild.
pdfjs-editor-add-signature-error-close-button = Schließen

## Dialog buttons

pdfjs-editor-add-signature-cancel-button = Abbrechen
pdfjs-editor-add-signature-add-button = Hinzufügen

## Main menu for adding/removing signatures

pdfjs-editor-delete-signature-button1 =
    .title = Gespeicherte Signatur entfernen
pdfjs-editor-delete-signature-button-label1 = Gespeicherte Signatur entfernen

## Editor toolbar

pdfjs-editor-add-signature-edit-button-label = Beschreibung bearbeiten

## Edit signature description dialog

pdfjs-editor-edit-signature-dialog-title = Beschreibung bearbeiten

## Dialog buttons

pdfjs-editor-edit-signature-update-button = Aktualisieren
