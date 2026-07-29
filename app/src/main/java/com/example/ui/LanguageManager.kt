package com.example.ui

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

data class LanguageOption(
    val nativeName: String,
    val engName: String,
    val code: String,
    val flag: String
)

object LanguageManager {

    val supportedLanguages = listOf(
        LanguageOption("English", "English", "en", "🇬🇧"),
        LanguageOption("Español", "Spanish", "es", "🇪🇸"),
        LanguageOption("हिंदी", "Hindi", "hi", "🇮🇳"),
        LanguageOption("Français", "French", "fr", "🇫🇷"),
        LanguageOption("Deutsch", "German", "de", "🇩🇪"),
        LanguageOption("日本語", "Japanese", "ja", "🇯🇵")
    )

    fun getLanguageCode(language: String): String {
        return when (language.trim().lowercase()) {
            "spanish", "español", "es" -> "es"
            "hindi", "हिंदी", "hi" -> "hi"
            "french", "français", "fr" -> "fr"
            "german", "deutsch", "de" -> "de"
            "japanese", "日本語", "ja" -> "ja"
            else -> "en"
        }
    }

    fun applyAppLocale(context: Context, language: String) {
        val code = getLanguageCode(language)
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    private val translations = mapOf(
        // Dock & Navigation
        "Dashboard" to mapOf(
            "en" to "Dashboard", "es" to "Inicio", "hi" to "डैशबोर्ड", "fr" to "Tableau de bord", "de" to "Übersicht", "ja" to "ダッシュボード"
        ),
        "Expenses" to mapOf(
            "en" to "Expenses", "es" to "Gastos", "hi" to "खर्च", "fr" to "Dépenses", "de" to "Ausgaben", "ja" to "支出"
        ),
        "Analytics" to mapOf(
            "en" to "Analytics", "es" to "Análisis", "hi" to "विश्लेषण", "fr" to "Analyses", "de" to "Analysen", "ja" to "分析"
        ),
        "Calendar" to mapOf(
            "en" to "Calendar", "es" to "Calendario", "hi" to "कैलेण्डर", "fr" to "Calendrier", "de" to "Kalender", "ja" to "カレンダー"
        ),
        "Settings" to mapOf(
            "en" to "Settings", "es" to "Ajustes", "hi" to "सेटिंग्स", "fr" to "Paramètres", "de" to "Einstellungen", "ja" to "設定"
        ),
        "Add" to mapOf(
            "en" to "Add", "es" to "Añadir", "hi" to "जोड़ें", "fr" to "Ajouter", "de" to "Hinzufügen", "ja" to "追加"
        ),

        // Dashboard & Overview Labels
        "Total Balance" to mapOf(
            "en" to "Total Balance", "es" to "Saldo Total", "hi" to "कुल शेष", "fr" to "Solde Total", "de" to "Gesamtsaldo", "ja" to "総残高"
        ),
        "Savings Goals" to mapOf(
            "en" to "Savings Goals", "es" to "Metas de Ahorro", "hi" to "बचत लक्ष्य", "fr" to "Objectifs d'épargne", "de" to "Sparziele", "ja" to "貯蓄 goal"
        ),
        "Create Goal" to mapOf(
            "en" to "Create Goal", "es" to "Crear Meta", "hi" to "लक्ष्य बनाएं", "fr" to "Créer un objectif", "de" to "Ziel erstellen", "ja" to "目標作成"
        ),
        "Add Savings Goal" to mapOf(
            "en" to "Add Savings Goal", "es" to "Añadir Meta de Ahorro", "hi" to "बचत लक्ष्य जोड़ें", "fr" to "Ajouter un objectif d'épargne", "de" to "Sparziel hinzufügen", "ja" to "貯蓄目標を追加"
        ),
        "Edit Savings Goal" to mapOf(
            "en" to "Edit Savings Goal", "es" to "Editar Meta de Ahorro", "hi" to "बचत लक्ष्य संपादित करें", "fr" to "Modifier l'objectif", "de" to "Sparziel bearbeiten", "ja" to "貯蓄目標を編集"
        ),
        "Deposit Money" to mapOf(
            "en" to "Deposit Money", "es" to "Depositar Dinero", "hi" to "पैसे जमा करें", "fr" to "Déposer de l'argent", "de" to "Geld einzahlen", "ja" to "入金する"
        ),
        "Total Income" to mapOf(
            "en" to "Total Income", "es" to "Ingresos Totales", "hi" to "कुल आय", "fr" to "Revenus Totaux", "de" to "Gesamteinnahmen", "ja" to "総収入"
        ),
        "Total Expenses" to mapOf(
            "en" to "Total Expenses", "es" to "Gastos Totales", "hi" to "कुल खर्च", "fr" to "Dépenses Totales", "de" to "Gesamtausgaben", "ja" to "総支出"
        ),
        "Budget Status" to mapOf(
            "en" to "Budget Status", "es" to "Estado del Presupuesto", "hi" to "बजट स्थिति", "fr" to "État du budget", "de" to "Budgetstatus", "ja" to "予算状況"
        ),
        "Adjust Budget" to mapOf(
            "en" to "Adjust Budget", "es" to "Ajustar Presupuesto", "hi" to "बजट समायोजित करें", "fr" to "Ajuster le budget", "de" to "Budget anpassen", "ja" to "予算調整"
        ),
        "Recent Activity" to mapOf(
            "en" to "Recent Activity", "es" to "Actividad Reciente", "hi" to "हाल की गतिविधि", "fr" to "Activité récente", "de" to "Letzte Aktivitäten", "ja" to "最近の履歴"
        ),
        "Quick Actions" to mapOf(
            "en" to "Quick Actions", "es" to "Acciones Rápidas", "hi" to "त्वरित कार्रवाई", "fr" to "Actions rapides", "de" to "Schnellaktionen", "ja" to "クイック操作"
        ),

        // Sidebar & Settings Drawer Labels
        "Settings & Preferences" to mapOf(
            "en" to "Settings & Preferences", "es" to "Ajustes y Preferencias", "hi" to "सेटिंग्स और प्राथमिकताएं", "fr" to "Paramètres & Préférences", "de" to "Einstellungen & Präferenzen", "ja" to "設定と設定項目"
        ),
        "App Configuration" to mapOf(
            "en" to "App Configuration", "es" to "Configuración de App", "hi" to "ऐप कॉन्फ़िगरेशन", "fr" to "Configuration de l'app", "de" to "App-Konfiguration", "ja" to "アプリ設定"
        ),
        "Data and Storage" to mapOf(
            "en" to "Data and Storage", "es" to "Datos y Almacenamiento", "hi" to "डेटा और स्टोरेज", "fr" to "Données et Stockage", "de" to "Daten & Speicher", "ja" to "データとストレージ"
        ),
        "Storage stats & clear cache" to mapOf(
            "en" to "Storage stats & clear cache", "es" to "Estadísticas de almacenamiento", "hi" to "स्टोरेज आँकड़े", "fr" to "Statistiques de stockage", "de" to "Speicherstatistiken", "ja" to "ストレージ統計"
        ),
        "Theme and Language" to mapOf(
            "en" to "Theme and Language", "es" to "Tema e Idioma", "hi" to "थीम और भाषा", "fr" to "Thème et Langue", "de" to "Design & Sprache", "ja" to "テーマと言語"
        ),
        "Dark theme & translations" to mapOf(
            "en" to "Dark theme & translations", "es" to "Modo oscuro y traducciones", "hi" to "डार्क मोड और अनुवाद", "fr" to "Mode sombre & traductions", "de" to "Dunkles Design & Übersetzungen", "ja" to "ダークテーマと翻訳"
        ),
        "Data Export & Reports" to mapOf(
            "en" to "Data Export & Reports", "es" to "Exportar Datos e Informes", "hi" to "डेटा निर्यात और रिपोर्ट", "fr" to "Exportation de données & Rapports", "de" to "Datenexport & Berichte", "ja" to "データ出力とレポート"
        ),
        "Export transactions to CSV or PDF" to mapOf(
            "en" to "Export transactions to CSV or PDF", "es" to "Exportar transacciones a CSV o PDF", "hi" to "लेन-देन को CSV या PDF में निर्यात करें", "fr" to "Exporter las transactions en CSV ou PDF", "de" to "Transaktionen als CSV oder PDF exportieren", "ja" to "取引履歴をCSVまたはPDFで出力"
        ),
        "FAQ and Help" to mapOf(
            "en" to "FAQ and Help", "es" to "Preguntas y Ayuda", "hi" to "प्रश्न और सहायता", "fr" to "FAQ et Aide", "de" to "FAQ & Hilfe", "ja" to "FAQとヘルプ"
        ),
        "FAQ & Help Support" to mapOf(
            "en" to "FAQ & Help Support", "es" to "Preguntas y Soporte de Ayuda", "hi" to "प्रश्न और सहायता समर्थन", "fr" to "FAQ & Support d'Aide", "de" to "FAQ & Hilfe-Support", "ja" to "FAQとサポート"
        ),
        "Guides, formulas & help docs" to mapOf(
            "en" to "Guides, formulas & help docs", "es" to "Guías, fórmulas y documentos", "hi" to "गाइड, सूत्र और सहायता दस्तावेज", "fr" to "Guides, formules & documents", "de" to "Anleitungen, Formeln & Hilfe", "ja" to "ガイドとヘルプドキュメント"
        ),
        "Offline Ledger Account" to mapOf(
            "en" to "Offline Ledger Account", "es" to "Cuenta de Contabilidad Local", "hi" to "ऑफलाइन लेजर खाता", "fr" to "Compte de comptabilité local", "de" to "Lokales Konto", "ja" to "オフライン帳簿アカウント"
        ),
        "Language Preference" to mapOf(
            "en" to "Language Preference", "es" to "Preferencia de Idioma", "hi" to "भाषा प्राथमिकता", "fr" to "Préférence de langue", "de" to "Sprachauswahl", "ja" to "言語設定"
        ),
        "Appearance & Dark Mode" to mapOf(
            "en" to "Appearance & Dark Mode", "es" to "Apariencia y Modo Oscuro", "hi" to "दिखावट और डार्क मोड", "fr" to "Apparence et Mode Sombre", "de" to "Erscheinungsbild & Dunkelmodus", "ja" to "外観とダークモード"
        ),
        "Dark Theme Mode" to mapOf(
            "en" to "Dark Theme Mode", "es" to "Modo de Tema Oscuro", "hi" to "डार्क थीम मोड", "fr" to "Mode Thème Sombre", "de" to "Dunkler Designmodus", "ja" to "ダークテーマモード"
        ),
        "Color Palette Presets" to mapOf(
            "en" to "Color Palette Presets", "es" to "Paletas de Colores", "hi" to "रंग पैलेट प्रीसेट", "fr" to "Palettes de couleurs", "de" to "Farbpaletten-Presets", "ja" to "カラーパレット"
        ),
        "Disk and network usage" to mapOf(
            "en" to "Disk and network usage", "es" to "Uso de disco y red", "hi" to "डिस्क और नेटवर्क उपयोग", "fr" to "Utilisation du disque et du réseau", "de" to "Speicher- und Netzwerknutzung", "ja" to "ディスクとネットワークの使用量"
        ),
        "Storage Usage" to mapOf(
            "en" to "Storage Usage", "es" to "Uso de Almacenamiento", "hi" to "स्टोरेज उपयोग", "fr" to "Utilisation du stockage", "de" to "Speichernutzung", "ja" to "ストレージ使用量"
        ),
        "Data Usage" to mapOf(
            "en" to "Data Usage", "es" to "Uso de Datos", "hi" to "डेटा उपयोग", "fr" to "Utilisation des données", "de" to "Datennutzung", "ja" to "データ使用量"
        ),
        "Local Offline Storage" to mapOf(
            "en" to "Local Offline Storage", "es" to "Almacenamiento Local Offline", "hi" to "लोकल ऑफलाइन स्टोरेज", "fr" to "Stockage local hors ligne", "de" to "Lokaler Offline-Speicher", "ja" to "ローカルオフラインストレージ"
        ),
        "Refresh Disk Usage Stats" to mapOf(
            "en" to "Refresh Disk Usage Stats", "es" to "Actualizar Estadísticas de Disco", "hi" to "डिस्क उपयोग आँकड़े ताज़ा करें", "fr" to "Actualiser les statistiques du disque", "de" to "Speicherstatistiken aktualisieren", "ja" to "ディスク使用量の再読み込み"
        ),
        "Export Ledger Statements" to mapOf(
            "en" to "Export Ledger Statements", "es" to "Exportar Estados de Cuenta", "hi" to "लेजर विवरण निर्यात करें", "fr" to "Exporter los relevés de compte", "de" to "Kontoauszüge exportieren", "ja" to "取引明細の出力"
        ),
        "Date Range Filter" to mapOf(
            "en" to "Date Range Filter", "es" to "Filtro de Rango de Fechas", "hi" to "तिथि सीमा फ़िल्टर", "fr" to "Filtre de plage de dates", "de" to "Datumsbereichsfilter", "ja" to "日付範囲フィルター"
        ),
        "Change Range" to mapOf(
            "en" to "Change Range", "es" to "Cambiar Rango", "hi" to "सीमा बदलें", "fr" to "Changer la plage", "de" to "Bereich ändern", "ja" to "範囲変更"
        ),
        "Transaction Type" to mapOf(
            "en" to "Transaction Type", "es" to "Tipo de Transacción", "hi" to "लेन-देन का प्रकार", "fr" to "Type de transaction", "de" to "Transaktionstyp", "ja" to "取引タイプ"
        ),
        "Full Ledger" to mapOf(
            "en" to "Full Ledger", "es" to "Libro Completo", "hi" to "पूरा लेजर", "fr" to "Registre complet", "de" to "Vollständiges Buch", "ja" to "全履歴"
        ),
        "Income Only" to mapOf(
            "en" to "Income Only", "es" to "Solo Ingresos", "hi" to "केवल आय", "fr" to "Revenus seulement", "de" to "Nur Einnahmen", "ja" to "収入のみ"
        ),
        "Expense Only" to mapOf(
            "en" to "Expense Only", "es" to "Solo Gastos", "hi" to "केवल खर्च", "fr" to "Dépenses seulement", "de" to "Nur Ausgaben", "ja" to "支出のみ"
        ),
        "Search transactions..." to mapOf(
            "en" to "Search transactions...", "es" to "Buscar transacciones...", "hi" to "लेन-देन खोजें...", "fr" to "Rechercher des transactions...", "de" to "Transaktionen suchen...", "ja" to "取引を検索..."
        ),
        "Filter" to mapOf(
            "en" to "Filter", "es" to "Filtrar", "hi" to "फ़िल्टर", "fr" to "Filtrer", "de" to "Filtern", "ja" to "フィルター"
        ),
        "Date Range" to mapOf(
            "en" to "Date Range", "es" to "Rango de Fechas", "hi" to "तिथि सीमा", "fr" to "Plage de dates", "de" to "Datumsbereich", "ja" to "日付範囲"
        ),
        "Export Formatted PDF Report" to mapOf(
            "en" to "Export Formatted PDF Report", "es" to "Exportar Informe PDF", "hi" to "PDF रिपोर्ट निर्यात करें", "fr" to "Exporter Rapport PDF", "de" to "PDF-Bericht exportieren", "ja" to "PDFレポート出力"
        ),
        "Export CSV File" to mapOf(
            "en" to "Export CSV File", "es" to "Exportar Archivo CSV", "hi" to "CSV फ़ाइल निर्यात करें", "fr" to "Exporter Fichier CSV", "de" to "CSV-Datei exportieren", "ja" to "CSV出力"
        )
    )

    fun tr(key: String, language: String): String {
        val code = getLanguageCode(language)
        val keyTranslations = translations[key] ?: return key
        return keyTranslations[code] ?: keyTranslations["en"] ?: key
    }
}
