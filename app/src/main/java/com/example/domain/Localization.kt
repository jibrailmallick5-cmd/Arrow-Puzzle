package com.example.domain

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    JAPANESE("ja", "日本語"),
    ARABIC("ar", "العربية");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

enum class GraphicsQuality(val label: String, val description: String) {
    HIGH("High", "Full 3D bevels, rich shadows & confetti particles"),
    BALANCED("Balanced", "Crisp 3D tiles with standard animations"),
    BATTERY_SAVER("Battery Saver", "Lightweight rendering for low-end devices");

    companion object {
        fun fromName(name: String): GraphicsQuality =
            entries.firstOrNull { it.name == name } ?: HIGH
    }
}

data class LocalizedStrings(
    val appTitle: String,
    val newGame: String,
    val continueLevel: String,
    val dailyChallenge: String,
    val tournament: String,
    val profile: String,
    val settings: String,
    val currentLevel: String,
    val hint: String,
    val ruler: String,
    val levelCompleted: String,
    val nextLevel: String,
    val mainMenu: String,
    val gameOver: String,
    val retryLevel: String,
    val skipTutorial: String,
    val tutorialWelcome: String,
    val tutorialTapFirst: String,
    val tutorialBlockedInfo: String,
    val tutorialSelectClear: String,
    val tutorialCompleted: String
)

object GameStrings {
    fun forLanguage(language: AppLanguage): LocalizedStrings = when (language) {
        AppLanguage.ENGLISH -> LocalizedStrings(
            appTitle = "Arrow Puzzle",
            newGame = "Play Now",
            continueLevel = "Continue",
            dailyChallenge = "Daily Challenge",
            tournament = "Tournament",
            profile = "Profile",
            settings = "Settings",
            currentLevel = "Current Level",
            hint = "Hint",
            ruler = "Ruler",
            levelCompleted = "Level Completed!",
            nextLevel = "Next Level",
            mainMenu = "Main Menu",
            gameOver = "Out of Hearts!",
            retryLevel = "Retry Level",
            skipTutorial = "Skip Tutorial",
            tutorialWelcome = "Welcome! Let's Learn How to Play.",
            tutorialTapFirst = "Tap this arrow to remove it.",
            tutorialBlockedInfo = "Blocked! An arrow cannot escape if another arrow blocks its path.",
            tutorialSelectClear = "Great! Now tap an arrow with a clear path to finish.",
            tutorialCompleted = "Tutorial Completed!"
        )
        AppLanguage.SPANISH -> LocalizedStrings(
            appTitle = "Arrow Puzzle",
            newGame = "Jugar",
            continueLevel = "Continuar",
            dailyChallenge = "Reto Diario",
            tournament = "Torneo",
            profile = "Perfil",
            settings = "Ajustes",
            currentLevel = "Nivel Actual",
            hint = "Pista",
            ruler = "Regla",
            levelCompleted = "¡Nivel Completado!",
            nextLevel = "Siguiente Nivel",
            mainMenu = "Menú Principal",
            gameOver = "¡Sin Vidas!",
            retryLevel = "Reintentar",
            skipTutorial = "Saltar Tutorial",
            tutorialWelcome = "¡Bienvenido! Aprendamos a jugar.",
            tutorialTapFirst = "Toca esta flecha para quitarla.",
            tutorialBlockedInfo = "¡Bloqueada! Una flecha no puede escapar si otra bloquea su camino.",
            tutorialSelectClear = "¡Genial! Ahora toca una flecha con el camino libre.",
            tutorialCompleted = "¡Tutorial Completado!"
        )
        AppLanguage.FRENCH -> LocalizedStrings(
            appTitle = "Arrow Puzzle",
            newGame = "Jouer",
            continueLevel = "Continuer",
            dailyChallenge = "Défi Quotidien",
            tournament = "Tournoi",
            profile = "Profil",
            settings = "Paramètres",
            currentLevel = "Niveau Actuel",
            hint = "Indice",
            ruler = "Règle",
            levelCompleted = "Niveau Terminé !",
            nextLevel = "Niveau Suivant",
            mainMenu = "Menu Principal",
            gameOver = "Plus de Cœurs !",
            retryLevel = "Réessayer",
            skipTutorial = "Passer",
            tutorialWelcome = "Bienvenue ! Apprenons à jouer.",
            tutorialTapFirst = "Appuyez sur cette flèche pour la retirer.",
            tutorialBlockedInfo = "Bloquée ! Une flèche ne peut pas sortir si une autre bloque son passage.",
            tutorialSelectClear = "Bravo ! Choisissez maintenant une flèche au passage libre.",
            tutorialCompleted = "Tutoriel Terminé !"
        )
        AppLanguage.GERMAN -> LocalizedStrings(
            appTitle = "Arrow Puzzle",
            newGame = "Spielen",
            continueLevel = "Fortsetzen",
            dailyChallenge = "Tages-Challenge",
            tournament = "Turnier",
            profile = "Profil",
            settings = "Einstellungen",
            currentLevel = "Aktuelles Level",
            hint = "Hinweis",
            ruler = "Lineal",
            levelCompleted = "Level Geschafft!",
            nextLevel = "Nächstes Level",
            mainMenu = "Hauptmenü",
            gameOver = "Keine Herzen mehr!",
            retryLevel = "Nochmal",
            skipTutorial = "Überspringen",
            tutorialWelcome = "Willkommen! Lass uns spielen lernen.",
            tutorialTapFirst = "Tippe auf diesen Pfeil, um ihn zu entfernen.",
            tutorialBlockedInfo = "Blockiert! Ein Pfeil kann nicht entkommen, wenn ein anderer im Weg ist.",
            tutorialSelectClear = "Super! Tippe nun auf einen Pfeil mit freiem Weg.",
            tutorialCompleted = "Tutorial Abgeschlossen!"
        )
        AppLanguage.JAPANESE -> LocalizedStrings(
            appTitle = "Arrow Puzzle",
            newGame = "プレイ",
            continueLevel = "つづきから",
            dailyChallenge = "デイリー",
            tournament = "トーナメント",
            profile = "プロフィール",
            settings = "設定",
            currentLevel = "現在のレベル",
            hint = "ヒント",
            ruler = "ルーラー",
            levelCompleted = "レベルクリア！",
            nextLevel = "次のレベル",
            mainMenu = "メインメニュー",
            gameOver = "ライフ切れ！",
            retryLevel = "リトライ",
            skipTutorial = "スキップ",
            tutorialWelcome = "ようこそ！遊び方を覚えましょう。",
            tutorialTapFirst = "この矢印をタップして消しましょう。",
            tutorialBlockedInfo = "ブロック中！他の矢印が邪魔をしていると脱出できません。",
            tutorialSelectClear = "ナイス！進路が空いている矢印を選びましょう。",
            tutorialCompleted = "チュートリアル完了！"
        )
        AppLanguage.ARABIC -> LocalizedStrings(
            appTitle = "Arrow Puzzle",
            newGame = "ابدأ اللعب",
            continueLevel = "متابعة",
            dailyChallenge = "التحدي اليومي",
            tournament = "البطولة",
            profile = "الملف الشخصي",
            settings = "الإعدادات",
            currentLevel = "المستوى الحالي",
            hint = "تلميح",
            ruler = "مسطرة",
            levelCompleted = "اكتمل المستوى!",
            nextLevel = "المستوى التالي",
            mainMenu = "القائمة الرئيسية",
            gameOver = "نفدت القلوب!",
            retryLevel = "إعادة المحاولة",
            skipTutorial = "تخطي الشرح",
            tutorialWelcome = "مرحبًا! لنتعلم طريقة اللعب.",
            tutorialTapFirst = "اضغط على هذا السهم لإزالته.",
            tutorialBlockedInfo = "مسدود! لا يمكن للسهم الهروب إذا كان هناك سهم آخر في طريقه.",
            tutorialSelectClear = "رائع! الآن اختر سهمًا طريقه مفتوح.",
            tutorialCompleted = "اكتمل الشرح!"
        )
    }
}
