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

        // Dashboard Labels
        "Total Balance" to mapOf(
            "en" to "Total Balance", "es" to "Saldo Total", "hi" to "कुल शेष", "fr" to "Solde Total", "de" to "Gesamtsaldo", "ja" to "総残高"
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

        // Settings Labels
        "Data & Storage" to mapOf(
            "en" to "Data & Storage", "es" to "Datos y Almacenamiento", "hi" to "डेटा और स्टोरेज", "fr" to "Données et Stockage", "de" to "Daten & Speicher", "ja" to "データとストレージ"
        ),
        "Theme and Language" to mapOf(
            "en" to "Theme and Language", "es" to "Tema e Idioma", "hi" to "थीम और भाषा", "fr" to "Thème et Langue", "de" to "Design & Sprache", "ja" to "テーマと言語"
        ),
        "Export Data" to mapOf(
            "en" to "Export Data", "es" to "Exportar Datos", "hi" to "डेटा निर्यात करें", "fr" to "Exporter las données", "de" to "Daten exportieren", "ja" to "データエクスポート"
        ),
        "FAQ & Support" to mapOf(
            "en" to "FAQ & Support", "es" to "Preguntas y Soporte", "hi" to "प्रश्न और सहायता", "fr" to "FAQ et Support", "de" to "FAQ & Support", "ja" to "FAQとサポート"
        ),
        "Language Preference" to mapOf(
            "en" to "Language Preference", "es" to "Preferencia de Idioma", "hi" to "भाषा प्राथमिकता", "fr" to "Préférence de langue", "de" to "Sprachauswahl", "ja" to "言語設定"
        ),
        "Dark Mode" to mapOf(
            "en" to "Dark Mode", "es" to "Modo Oscuro", "hi" to "डार्क मोड", "fr" to "Mode Sombre", "de" to "Dunkelmodus", "ja" to "ダークモード"
        ),
        "Primary Theme Accent" to mapOf(
            "en" to "Primary Theme Accent", "es" to "Acento del Tema", "hi" to "मुख्य थीम रंग", "fr" to "Couleur du thème", "de" to "Hauptakzentfarbe", "ja" to "テーマアクセント"
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
