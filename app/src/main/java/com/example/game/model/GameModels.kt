package com.example.game.model

enum class GameScreen {
    MAIN_MENU,
    CAMPAIGN,
    CLASS_LOADOUT,
    SHOP,
    SETTINGS,
    BATTLE
}

enum class Faction {
    GREAT_BRITAIN,
    OTTOMAN_EMPIRE
}

enum class WeaponType {
    BOLT_RIFLE,
    TRENCH_SMG,
    LIGHT_MG,
    SCOPED_RIFLE,
    OFFICER_REVOLVER,
    TRENCH_SHOTGUN,
    MAUSER_PISTOL,
    STRIP_LMG
}

enum class UniformStyle(
    val id: String,
    val titleRu: String,
    val coatColorHex: Long,
    val trouserColorHex: Long,
    val helmetColorHex: Long,
    val descriptionRu: String,
    val priceStars: Int = 0
) {
    BRITISH_KHAKI(
        id = "british_khaki",
        titleRu = "Британский хаки (1915)",
        coatColorHex = 0xFF827553,
        trouserColorHex = 0xFF706446,
        helmetColorHex = 0xFF5D5740,
        descriptionRu = "Классическая форма Британской империи с каской Броди.",
        priceStars = 0
    ),
    GALLIPOLI_DESERT(
        id = "gallipoli_desert",
        titleRu = "Песочный корпус АНЗАК",
        coatColorHex = 0xFFA38F68,
        trouserColorHex = 0xFF8C7A56,
        helmetColorHex = 0xFF736548,
        descriptionRu = "Облегчённая пустынная форма для берега Галлиполи и Газы.",
        priceStars = 110
    ),
    ROYAL_GUARD_SLATE(
        id = "royal_guard_slate",
        titleRu = "Королевский штурмовой",
        coatColorHex = 0xFF4F5961,
        trouserColorHex = 0xFF3E474E,
        helmetColorHex = 0xFF373F45,
        descriptionRu = "Тёмная уличная разгрузка для городских боёв в сумерках.",
        priceStars = 160
    ),
    OFFICER_OLIVE(
        id = "officer_olive",
        titleRu = "Полевой мундир штаба",
        coatColorHex = 0xFF5E694C,
        trouserColorHex = 0xFF4D573E,
        helmetColorHex = 0xFF454D38,
        descriptionRu = "Усиленная шинель командного состава британских экспедиционных сил.",
        priceStars = 210
    )
}

data class WeaponSpec(
    val id: String,
    val nameRu: String,
    val subtitleRu: String,
    val type: WeaponType,
    val damage: Int,         // 1..100
    val accuracy: Int,       // 1..100
    val fireRate: Int,       // 1..100
    val range: Int,          // 1..100
    val magazineSize: Int,
    val reserveAmmo: Int,
    val fireIntervalSec: Float,
    val reloadDurationSec: Float,
    val adsFov: Float,
    val baseSpread: Float,
    val isAutomatic: Boolean,
    val pelletCount: Int = 1,
    val priceStars: Int = 0
)

enum class ShopConsumable(
    val id: String,
    val titleRu: String,
    val descriptionRu: String,
    val priceStars: Int
) {
    MEDKIT(
        id = "medkit",
        titleRu = "Полевой бинт (Аптечка)",
        descriptionRu = "Мгновенно восстанавливает +45 ОЗ прямо во время боя.",
        priceStars = 50
    ),
    ARMOR_PLATE(
        id = "armor_plate",
        titleRu = "Нагрудная бронепластина",
        descriptionRu = "+40 к максимальному запасу прочности на следующий бой.",
        priceStars = 85
    ),
    ORDNANCE_PACK(
        id = "ordnance_pack",
        titleRu = "Ящик гранат и патронов",
        descriptionRu = "+60% боезапаса и мгновенный заряд спецспособности в бою.",
        priceStars = 75
    )
}

data class PerkSpec(
    val id: String,
    val titleRu: String,
    val effectRu: String
)

enum class SoldierClass(
    val id: String,
    val titleRu: String,
    val categoryRu: String,
    val roleSummaryRu: String,
    val specialAbilityRu: String,
    val specialCooldownSec: Float,
    val maxHealth: Int,
    val moveSpeed: Float,
    val defaultWeaponId: String,
    val altWeaponId: String,
    val defaultUniform: UniformStyle
) {
    OFFICER(
        id = "officer",
        titleRu = "ОФИЦЕР",
        categoryRu = "КОМАНДОВАНИЕ",
        roleSummaryRu = "Вызывает залп корабельной/полевой артиллерии и поднимает отряд свистком.",
        specialAbilityRu = "АРТОБСТРЕЛ",
        specialCooldownSec = 13f,
        maxHealth = 115,
        moveSpeed = 5.6f,
        defaultWeaponId = "webley_mk6",
        altWeaponId = "mauser_c96",
        defaultUniform = UniformStyle.OFFICER_OLIVE
    ),
    RIFLEMAN(
        id = "rifleman",
        titleRu = "СТРЕЛОК",
        categoryRu = "ПЕХОТА",
        roleSummaryRu = "Основной пехотинец Британской армии. Точная магазинная винтовка Ли-Энфилд или Маузер.",
        specialAbilityRu = "ГРАНАТА MILLS",
        specialCooldownSec = 8.5f,
        maxHealth = 120,
        moveSpeed = 5.5f,
        defaultWeaponId = "lee_enfield",
        altWeaponId = "gewehr_98",
        defaultUniform = UniformStyle.BRITISH_KHAKI
    ),
    ASSAULT(
        id = "assault",
        titleRu = "ШТУРМОВИК",
        categoryRu = "ГОРОДСКОЙ ШТУРМ",
        roleSummaryRu = "Зачищает узкие улочки и окопы из дробовика Winchester M1897 или ПП MP-18.",
        specialAbilityRu = "ШТУРМ-ГРАНАТА",
        specialCooldownSec = 7f,
        maxHealth = 125,
        moveSpeed = 6.0f,
        defaultWeaponId = "mp18_trench",
        altWeaponId = "winchester_1897",
        defaultUniform = UniformStyle.GALLIPOLI_DESERT
    ),
    MG_GUNNER(
        id = "mg_gunner",
        titleRu = "ПУЛЕМЁТЧИК",
        categoryRu = "ПОДДЕРЖКА",
        roleSummaryRu = "Подавляет османские укрепления плотным огнём пулемёта Льюиса или Гочкиса.",
        specialAbilityRu = "ОСКОЛОЧНЫЙ ЗАЛП",
        specialCooldownSec = 10.5f,
        maxHealth = 140,
        moveSpeed = 4.7f,
        defaultWeaponId = "lewis_gun",
        altWeaponId = "hotchkiss_m1909",
        defaultUniform = UniformStyle.BRITISH_KHAKI
    ),
    ENGINEER(
        id = "engineer",
        titleRu = "ИНЖЕНЕР",
        categoryRu = "САПЁР",
        roleSummaryRu = "Восстанавливает здоровье и боезапас в уличных и окопных боях.",
        specialAbilityRu = "ПОЛЕВАЯ АПТЕЧКА",
        specialCooldownSec = 10f,
        maxHealth = 130,
        moveSpeed = 5.3f,
        defaultWeaponId = "lee_enfield_carbine",
        altWeaponId = "winchester_1897",
        defaultUniform = UniformStyle.ROYAL_GUARD_SLATE
    ),
    SNIPER(
        id = "sniper",
        titleRu = "СНАЙПЕР",
        categoryRu = "РАЗВЕДКА",
        roleSummaryRu = "Оптический прицел 3.5x позволяет снимать стрелков с крыш и укреплений.",
        specialAbilityRu = "ДЫМОВАЯ ЗАВЕСА",
        specialCooldownSec = 11.5f,
        maxHealth = 100,
        moveSpeed = 5.6f,
        defaultWeaponId = "smle_scoped",
        altWeaponId = "ross_mk3_scoped",
        defaultUniform = UniformStyle.GALLIPOLI_DESERT
    )
}

enum class TheatreBiome {
    GALLIPOLI_COAST,
    BASRA_OUTSKIRTS,
    GAZA_CITY,
    JERUSALEM_CITADEL
}

data class CampaignChapter(
    val index: Int,
    val id: String,
    val titleRu: String,
    val dateAndFrontRu: String,
    val theatre: TheatreBiome,
    val subtitleRu: String,
    val storyDispatchRu: String,
    val objectiveTitleRu: String,
    val targetKills: Int,
    val sectorsToCapture: Int,
    val recommendedClass: SoldierClass
)

object GameCatalog {
    val weapons: Map<String, WeaponSpec> = listOf(
        WeaponSpec(
            id = "lee_enfield",
            nameRu = "SMLE Mk III (Ли-Энфилд)",
            subtitleRu = "Британская винтовка .303, 10 патр.",
            type = WeaponType.BOLT_RIFLE,
            damage = 84,
            accuracy = 90,
            fireRate = 48,
            range = 88,
            magazineSize = 10,
            reserveAmmo = 60,
            fireIntervalSec = 0.66f,
            reloadDurationSec = 1.9f,
            adsFov = 39f,
            baseSpread = 0.004f,
            isAutomatic = false,
            priceStars = 0
        ),
        WeaponSpec(
            id = "gewehr_98",
            nameRu = "Mauser Gewehr 98 (Осман. трофей)",
            subtitleRu = "Длинноствольная винтовка 7.92 мм",
            type = WeaponType.BOLT_RIFLE,
            damage = 94,
            accuracy = 94,
            fireRate = 38,
            range = 94,
            magazineSize = 5,
            reserveAmmo = 45,
            fireIntervalSec = 0.78f,
            reloadDurationSec = 1.85f,
            adsFov = 36f,
            baseSpread = 0.003f,
            isAutomatic = false,
            priceStars = 150
        ),
        WeaponSpec(
            id = "lee_enfield_carbine",
            nameRu = "SMLE Карабин сапёра",
            subtitleRu = "Укороченный окопный карабин",
            type = WeaponType.BOLT_RIFLE,
            damage = 76,
            accuracy = 83,
            fireRate = 54,
            range = 74,
            magazineSize = 10,
            reserveAmmo = 50,
            fireIntervalSec = 0.56f,
            reloadDurationSec = 1.65f,
            adsFov = 42f,
            baseSpread = 0.006f,
            isAutomatic = false,
            priceStars = 0
        ),
        WeaponSpec(
            id = "mp18_trench",
            nameRu = "MP-18 Траншейный",
            subtitleRu = "Пистолет-пулемёт 9 мм, 32 патр.",
            type = WeaponType.TRENCH_SMG,
            damage = 35,
            accuracy = 64,
            fireRate = 88,
            range = 44,
            magazineSize = 32,
            reserveAmmo = 160,
            fireIntervalSec = 0.11f,
            reloadDurationSec = 2.0f,
            adsFov = 48f,
            baseSpread = 0.022f,
            isAutomatic = true,
            priceStars = 0
        ),
        WeaponSpec(
            id = "winchester_1897",
            nameRu = "Winchester M1897 «Окопная метла»",
            subtitleRu = "Помповый дробовик (6 картечин)",
            type = WeaponType.TRENCH_SHOTGUN,
            damage = 28,
            accuracy = 52,
            fireRate = 50,
            range = 34,
            magazineSize = 6,
            reserveAmmo = 36,
            fireIntervalSec = 0.65f,
            reloadDurationSec = 2.1f,
            adsFov = 52f,
            baseSpread = 0.068f,
            isAutomatic = false,
            pelletCount = 6,
            priceStars = 180
        ),
        WeaponSpec(
            id = "lewis_gun",
            nameRu = "Пулемёт Льюиса Mk I",
            subtitleRu = "Британский ручной пулемёт, 47 патр.",
            type = WeaponType.LIGHT_MG,
            damage = 44,
            accuracy = 72,
            fireRate = 80,
            range = 74,
            magazineSize = 47,
            reserveAmmo = 188,
            fireIntervalSec = 0.14f,
            reloadDurationSec = 2.5f,
            adsFov = 44f,
            baseSpread = 0.017f,
            isAutomatic = true,
            priceStars = 0
        ),
        WeaponSpec(
            id = "hotchkiss_m1909",
            nameRu = "Hotchkiss M1909",
            subtitleRu = "Ленточный пулемёт с радиатором",
            type = WeaponType.STRIP_LMG,
            damage = 54,
            accuracy = 78,
            fireRate = 70,
            range = 82,
            magazineSize = 30,
            reserveAmmo = 180,
            fireIntervalSec = 0.17f,
            reloadDurationSec = 2.2f,
            adsFov = 42f,
            baseSpread = 0.013f,
            isAutomatic = true,
            priceStars = 230
        ),
        WeaponSpec(
            id = "smle_scoped",
            nameRu = "SMLE Снайперская",
            subtitleRu = "Винтовка с оптикой 3.5x",
            type = WeaponType.SCOPED_RIFLE,
            damage = 100,
            accuracy = 98,
            fireRate = 32,
            range = 98,
            magazineSize = 5,
            reserveAmmo = 35,
            fireIntervalSec = 0.96f,
            reloadDurationSec = 2.1f,
            adsFov = 19f,
            baseSpread = 0.001f,
            isAutomatic = false,
            priceStars = 0
        ),
        WeaponSpec(
            id = "ross_mk3_scoped",
            nameRu = "Ross Mk III (Оптика)",
            subtitleRu = "Канадская снайперская винтовка",
            type = WeaponType.SCOPED_RIFLE,
            damage = 115,
            accuracy = 100,
            fireRate = 40,
            range = 100,
            magazineSize = 5,
            reserveAmmo = 40,
            fireIntervalSec = 0.78f,
            reloadDurationSec = 1.95f,
            adsFov = 17f,
            baseSpread = 0.0008f,
            isAutomatic = false,
            priceStars = 260
        ),
        WeaponSpec(
            id = "webley_mk6",
            nameRu = "Webley Mk VI",
            subtitleRu = "Британский револьвер .455",
            type = WeaponType.OFFICER_REVOLVER,
            damage = 64,
            accuracy = 78,
            fireRate = 62,
            range = 48,
            magazineSize = 6,
            reserveAmmo = 48,
            fireIntervalSec = 0.34f,
            reloadDurationSec = 1.55f,
            adsFov = 48f,
            baseSpread = 0.010f,
            isAutomatic = false,
            priceStars = 0
        ),
        WeaponSpec(
            id = "mauser_c96",
            nameRu = "Mauser C96 (Кобура-приклад)",
            subtitleRu = "Полуавтоматический пистолет-карабин",
            type = WeaponType.MAUSER_PISTOL,
            damage = 58,
            accuracy = 84,
            fireRate = 76,
            range = 60,
            magazineSize = 10,
            reserveAmmo = 70,
            fireIntervalSec = 0.22f,
            reloadDurationSec = 1.5f,
            adsFov = 45f,
            baseSpread = 0.008f,
            isAutomatic = false,
            priceStars = 140
        )
    ).associateBy { it.id }

    val perks: List<PerkSpec> = listOf(
        PerkSpec("hardened", "Закалённый боец", "+15% сопротивления осколкам"),
        PerkSpec("fast_bolt", "Быстрый затвор", "+20% скорость перезарядки"),
        PerkSpec("steady_aim", "Стальные нервы", "Меньше отдача при стрельбе"),
        PerkSpec("eagle_eye", "Орлиный глаз", "Улучшенная точность прицеливания"),
        PerkSpec("field_medic", "Санитарная сумка", "Автовосстановление в укрытии"),
        PerkSpec("bayonet_charge", "Штыковой бросок", "+10% скорость рывка в атаку")
    )

    val chapters: List<CampaignChapter> = listOf(
        CampaignChapter(
            index = 0,
            id = "gallipoli_1915",
            titleRu = "ГЛАВА I: ВЫСАДКА НА ГАЛЛИПОЛИ",
            dateAndFrontRu = "Апрель 1915 • Британия против Османской империи",
            theatre = TheatreBiome.GALLIPOLI_COAST,
            subtitleRu = "Десант Королевских войск на мысе Геллес",
            storyDispatchRu = "Британский транспортный пароход дымит на рейде за спиной. Скалистые высоты удерживают османские батальоны под красным полумесяцем. Пробейтесь через колючую проволоку и поднимите Юнион Джек над береговыми окопами!",
            objectiveTitleRu = "Захватить османские позиции на берегу",
            targetKills = 12,
            sectorsToCapture = 2,
            recommendedClass = SoldierClass.RIFLEMAN
        ),
        CampaignChapter(
            index = 1,
            id = "basra_1916",
            titleRu = "ГЛАВА II: ПРИГОРОД КУТ-ЭЛЬ-АМАРЫ",
            dateAndFrontRu = "Март 1916 • Месопотамский фронт",
            theatre = TheatreBiome.BASRA_OUTSKIRTS,
            subtitleRu = "Глинобитные укрепления и пальмовые рощи",
            storyDispatchRu = "Жара Месопотамии и плотный огонь османской пехоты сковали наступление вдоль берега Тигра. Займите передовые глинобитные постройки и проложите путь колонне снабжения.",
            objectiveTitleRu = "Очистить окраину и занять оба сектора",
            targetKills = 15,
            sectorsToCapture = 2,
            recommendedClass = SoldierClass.MG_GUNNER
        ),
        CampaignChapter(
            index = 2,
            id = "gaza_city_1917",
            titleRu = "ГЛАВА III: УЛИЧНЫЕ БОИ В ГАЗЕ",
            dateAndFrontRu = "Ноябрь 1917 • Городской штурм",
            theatre = TheatreBiome.GAZA_CITY,
            subtitleRu = "Сражение за целый город: базар, улицы и башня",
            storyDispatchRu = "Бой перешёл в густую городскую застройку! Османские стрелки укрылись на перекрёстках, за баррикадами из мешков и у старой рыночной площади. Продвигайтесь по улицам от квартала к кварталу и поднимите британские флаги над городом!",
            objectiveTitleRu = "Занять рыночную площадь, центр и башню",
            targetKills = 18,
            sectorsToCapture = 3,
            recommendedClass = SoldierClass.ASSAULT
        ),
        CampaignChapter(
            index = 3,
            id = "jerusalem_1918",
            titleRu = "ГЛАВА IV: ЦИТАДЕЛЬ И ГОРОД МЕГИДДО",
            dateAndFrontRu = "Сентябрь 1918 • Решающий прорыв",
            theatre = TheatreBiome.JERUSALEM_CITADEL,
            subtitleRu = "Финальный штурм каменного города-крепости",
            storyDispatchRu = "Древние каменные кварталы, арочные галереи и главная площадь крепости стали последним оплотом османского штаба. Соберите весь отряд в единый кулак и завершите кампанию полной победой!",
            objectiveTitleRu = "Захватить все 3 квартала и штабную цитадель",
            targetKills = 22,
            sectorsToCapture = 3,
            recommendedClass = SoldierClass.OFFICER
        )
    )
}
