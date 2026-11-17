package com.example.game.model

enum class GameScreen {
    MAIN_MENU,
    CAMPAIGN,
    CLASS_LOADOUT,
    SETTINGS,
    BATTLE
}

enum class WeaponType {
    BOLT_RIFLE,
    TRENCH_SMG,
    LIGHT_MG,
    SCOPED_RIFLE,
    OFFICER_REVOLVER
}

enum class UniformStyle(
    val id: String,
    val titleRu: String,
    val coatColorHex: Long,
    val trouserColorHex: Long,
    val helmetColorHex: Long,
    val descriptionRu: String
) {
    HORIZON_BLUE(
        id = "horizon_blue",
        titleRu = "Голубой горизонт (1915)",
        coatColorHex = 0xFF5D7FA3,
        trouserColorHex = 0xFF4C6B8A,
        helmetColorHex = 0xFF475C70,
        descriptionRu = "Классическая шинель и каска Адриана армии Антанты."
    ),
    KHAKI_EXPEDITION(
        id = "khaki_expedition",
        titleRu = "Экспедиционный хаки",
        coatColorHex = 0xFF7D7452,
        trouserColorHex = 0xFF6A6243,
        helmetColorHex = 0xFF5B563E,
        descriptionRu = "Полевая форма штурмовых частей побережья и Галлиполи."
    ),
    ALPINE_SLATE(
        id = "alpine_slate",
        titleRu = "Альпийский серый",
        coatColorHex = 0xFF5C6660,
        trouserColorHex = 0xFF4A524D,
        helmetColorHex = 0xFF404744,
        descriptionRu = "Утеплённая горная форма для боёв на хребте Изонцо."
    ),
    STORM_DARK(
        id = "storm_dark",
        titleRu = "Штурмовой камуфляж",
        coatColorHex = 0xFF3E4448,
        trouserColorHex = 0xFF32363A,
        helmetColorHex = 0xFF2D3134,
        descriptionRu = "Усиленная разгрузка для ночных рейдов на траншеи."
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
    val isAutomatic: Boolean
)

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
        roleSummaryRu = "Вызывает артудар по позициям врага и поднимает боевой дух отряда свистком.",
        specialAbilityRu = "АРТОБСТРЕЛ",
        specialCooldownSec = 14f,
        maxHealth = 110,
        moveSpeed = 5.6f,
        defaultWeaponId = "webley_mk6",
        altWeaponId = "lebel_carbine",
        defaultUniform = UniformStyle.HORIZON_BLUE
    ),
    RIFLEMAN(
        id = "rifleman",
        titleRu = "СТРЕЛОК",
        categoryRu = "ПЕХОТА",
        roleSummaryRu = "Универсальный боец передовой. Точная магазинная винтовка со штыком и траншейной лопатой.",
        specialAbilityRu = "ГРАНАТА F1",
        specialCooldownSec = 9f,
        maxHealth = 115,
        moveSpeed = 5.4f,
        defaultWeaponId = "lebel_1886",
        altWeaponId = "lee_enfield",
        defaultUniform = UniformStyle.HORIZON_BLUE
    ),
    ASSAULT(
        id = "assault",
        titleRu = "ШТУРМОВИК",
        categoryRu = "ШТУРМ",
        roleSummaryRu = "Мастер ближнего боя в узких окопах. Пистолет-пулемёт и связка осколочных гранат.",
        specialAbilityRu = "ШТУРМ-ГРАНАТА",
        specialCooldownSec = 7f,
        maxHealth = 120,
        moveSpeed = 6.0f,
        defaultWeaponId = "mp18_trench",
        altWeaponId = "carcano_moschetto",
        defaultUniform = UniformStyle.STORM_DARK
    ),
    MG_GUNNER(
        id = "mg_gunner",
        titleRu = "ПУЛЕМЁТЧИК",
        categoryRu = "ПОДДЕРЖКА",
        roleSummaryRu = "Подавляет вражеские атаки шквальным огнём из ручного пулемёта с дисковым магазином.",
        specialAbilityRu = "ОСКОЛОЧНЫЙ ЗАЛП",
        specialCooldownSec = 11f,
        maxHealth = 135,
        moveSpeed = 4.6f,
        defaultWeaponId = "lewis_gun",
        altWeaponId = "chauchat_1915",
        defaultUniform = UniformStyle.KHAKI_EXPEDITION
    ),
    ENGINEER(
        id = "engineer",
        titleRu = "ИНЖЕНЕР",
        categoryRu = "САПЁР",
        roleSummaryRu = "Восстанавливает здоровье и боезапас полевым комплектом. Эффективен в удержании сектора.",
        specialAbilityRu = "ПОЛЕВАЯ АПТЕЧКА",
        specialCooldownSec = 10f,
        maxHealth = 125,
        moveSpeed = 5.2f,
        defaultWeaponId = "carcano_moschetto",
        altWeaponId = "lee_enfield",
        defaultUniform = UniformStyle.ALPINE_SLATE
    ),
    SNIPER(
        id = "sniper",
        titleRu = "СНАЙПЕР",
        categoryRu = "РАЗВЕДКА",
        roleSummaryRu = "Оптический прицел 3.5x позволяет поражать цели на дальней дистанции одним точным выстрелом.",
        specialAbilityRu = "ДЫМОВАЯ ЗАВЕСА",
        specialCooldownSec = 12f,
        maxHealth = 95,
        moveSpeed = 5.5f,
        defaultWeaponId = "smle_scoped",
        altWeaponId = "lebel_1886",
        defaultUniform = UniformStyle.ALPINE_SLATE
    )
}

enum class TheatreBiome {
    MARNE_TRENCHES,
    VERDUN_CRATERS,
    COASTAL_LANDING,
    ALPINE_FORTRESS
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
            id = "lebel_1886",
            nameRu = "Lebel M1886",
            subtitleRu = "Магазинная винтовка 8 мм",
            type = WeaponType.BOLT_RIFLE,
            damage = 88,
            accuracy = 92,
            fireRate = 34,
            range = 90,
            magazineSize = 8,
            reserveAmmo = 48,
            fireIntervalSec = 0.85f,
            reloadDurationSec = 2.1f,
            adsFov = 38f,
            baseSpread = 0.004f,
            isAutomatic = false
        ),
        WeaponSpec(
            id = "lee_enfield",
            nameRu = "SMLE Mk III",
            subtitleRu = "Быстрозарядная винтовка .303",
            type = WeaponType.BOLT_RIFLE,
            damage = 80,
            accuracy = 88,
            fireRate = 45,
            range = 85,
            magazineSize = 10,
            reserveAmmo = 60,
            fireIntervalSec = 0.68f,
            reloadDurationSec = 1.9f,
            adsFov = 40f,
            baseSpread = 0.005f,
            isAutomatic = false
        ),
        WeaponSpec(
            id = "lebel_carbine",
            nameRu = "Berthier Карабин",
            subtitleRu = "Облегчённый окопный карабин",
            type = WeaponType.BOLT_RIFLE,
            damage = 74,
            accuracy = 82,
            fireRate = 52,
            range = 75,
            magazineSize = 5,
            reserveAmmo = 45,
            fireIntervalSec = 0.58f,
            reloadDurationSec = 1.6f,
            adsFov = 42f,
            baseSpread = 0.007f,
            isAutomatic = false
        ),
        WeaponSpec(
            id = "mp18_trench",
            nameRu = "MP-18 Траншейный",
            subtitleRu = "Пистолет-пулемёт 9 мм",
            type = WeaponType.TRENCH_SMG,
            damage = 34,
            accuracy = 62,
            fireRate = 88,
            range = 42,
            magazineSize = 32,
            reserveAmmo = 160,
            fireIntervalSec = 0.11f,
            reloadDurationSec = 2.0f,
            adsFov = 48f,
            baseSpread = 0.024f,
            isAutomatic = true
        ),
        WeaponSpec(
            id = "carcano_moschetto",
            nameRu = "Carcano Moschetto",
            subtitleRu = "Штурмовой карабин со штыком",
            type = WeaponType.BOLT_RIFLE,
            damage = 76,
            accuracy = 84,
            fireRate = 50,
            range = 78,
            magazineSize = 6,
            reserveAmmo = 54,
            fireIntervalSec = 0.62f,
            reloadDurationSec = 1.7f,
            adsFov = 42f,
            baseSpread = 0.006f,
            isAutomatic = false
        ),
        WeaponSpec(
            id = "lewis_gun",
            nameRu = "Пулемёт Льюиса",
            subtitleRu = "Ручной пулемёт, диск 47 патр.",
            type = WeaponType.LIGHT_MG,
            damage = 44,
            accuracy = 70,
            fireRate = 80,
            range = 72,
            magazineSize = 47,
            reserveAmmo = 188,
            fireIntervalSec = 0.14f,
            reloadDurationSec = 2.6f,
            adsFov = 44f,
            baseSpread = 0.018f,
            isAutomatic = true
        ),
        WeaponSpec(
            id = "chauchat_1915",
            nameRu = "CSRG Chauchat 1915",
            subtitleRu = "Тяжёлый ручной пулемёт 8 мм",
            type = WeaponType.LIGHT_MG,
            damage = 54,
            accuracy = 66,
            fireRate = 68,
            range = 76,
            magazineSize = 20,
            reserveAmmo = 140,
            fireIntervalSec = 0.21f,
            reloadDurationSec = 2.3f,
            adsFov = 44f,
            baseSpread = 0.020f,
            isAutomatic = true
        ),
        WeaponSpec(
            id = "smle_scoped",
            nameRu = "SMLE Снайперская",
            subtitleRu = "Винтовка с оптикой 3.5x",
            type = WeaponType.SCOPED_RIFLE,
            damage = 100,
            accuracy = 99,
            fireRate = 30,
            range = 100,
            magazineSize = 5,
            reserveAmmo = 35,
            fireIntervalSec = 1.0f,
            reloadDurationSec = 2.2f,
            adsFov = 19f,
            baseSpread = 0.001f,
            isAutomatic = false
        ),
        WeaponSpec(
            id = "webley_mk6",
            nameRu = "Webley Mk VI",
            subtitleRu = "Офицерский револьвер .455",
            type = WeaponType.OFFICER_REVOLVER,
            damage = 64,
            accuracy = 78,
            fireRate = 62,
            range = 48,
            magazineSize = 6,
            reserveAmmo = 48,
            fireIntervalSec = 0.34f,
            reloadDurationSec = 1.6f,
            adsFov = 48f,
            baseSpread = 0.010f,
            isAutomatic = false
        )
    ).associateBy { it.id }

    val perks: List<PerkSpec> = listOf(
        PerkSpec("hardened", "Закалённый боец", "+15% сопротивления осколкам"),
        PerkSpec("fast_bolt", "Быстрый затвор", "+20% скорость перезарядки"),
        PerkSpec("steady_aim", "Стальные нервы", "Меньше отдача при стрельбе"),
        PerkSpec("eagle_eye", "Орлиный глаз", "Улучшенная дальность обнаружения"),
        PerkSpec("field_medic", "Санитарная сумка", "Автовосстановление в укрытии"),
        PerkSpec("bayonet_charge", "Боевой порыв", "+10% скорость рывка в атаку")
    )

    val chapters: List<CampaignChapter> = listOf(
        CampaignChapter(
            index = 0,
            id = "marne_1914",
            titleRu = "ГЛАВА I: РУБЕЖ МАРНЫ",
            dateAndFrontRu = "Сентябрь 1914 • Западный фронт",
            theatre = TheatreBiome.MARNE_TRENCHES,
            subtitleRu = "Первая линия обороны перед Парижем",
            storyDispatchRu = "Артиллерия противника утюжит наши передовые траншеи с рассвета. Командование приказало удержать деревянные укрепления любой ценой и отбросить передовые цепи неприятеля за нейтральную полосу.",
            objectiveTitleRu = "Отразить волны и захватить передовой пост",
            targetKills = 12,
            sectorsToCapture = 2,
            recommendedClass = SoldierClass.RIFLEMAN
        ),
        CampaignChapter(
            index = 1,
            id = "verdun_1916",
            titleRu = "ГЛАВА II: ПЕКЛО ВЕРДЕНА",
            dateAndFrontRu = "Февраль 1916 • Форт Дуомон",
            theatre = TheatreBiome.VERDUN_CRATERS,
            subtitleRu = "Переход через изрытую воронками землю",
            storyDispatchRu = "Вокруг лишь грязь, колючая проволока и непрекращающийся грохот гаубиц. Пробейтесь через ничейную землю от воронки к воронке и подавите вражеский пулемётный расчёт у бетонного ДОТа.",
            objectiveTitleRu = "Прорвать оборону и занять оба сектора",
            targetKills = 16,
            sectorsToCapture = 2,
            recommendedClass = SoldierClass.ASSAULT
        ),
        CampaignChapter(
            index = 2,
            id = "gallipoli_1915",
            titleRu = "ГЛАВА III: ВЫСАДКА НА ПОБЕРЕЖЬЕ",
            dateAndFrontRu = "Апрель 1915 • Мыс Геллес",
            theatre = TheatreBiome.COASTAL_LANDING,
            subtitleRu = "Штурм скалистых береговых укреплений",
            storyDispatchRu = "Десантный пароход дымит на рейде за спиной, а каменистый склон впереди перетянут рядами колючей проволоки. Поднимайтесь вверх от окопа к окопу, прикрывая товарищей точным огнём.",
            objectiveTitleRu = "Захватить береговую батарею на высоте",
            targetKills = 18,
            sectorsToCapture = 3,
            recommendedClass = SoldierClass.RIFLEMAN
        ),
        CampaignChapter(
            index = 3,
            id = "isonzo_1917",
            titleRu = "ГЛАВА IV: ХРЕБЕТ ИЗОНЦО",
            dateAndFrontRu = "Август 1917 • Альпийский фронт",
            theatre = TheatreBiome.ALPINE_FORTRESS,
            subtitleRu = "Решающий штурм каменной крепости",
            storyDispatchRu = "Холодный горный ветер несёт эхо выстрелов вдоль отвесных скал. Враг засел за древними каменными стенами. Соберите всю волю в кулак — этот прорыв решит исход всей кампании!",
            objectiveTitleRu = "Сокрушить штабную линию в горах",
            targetKills = 22,
            sectorsToCapture = 3,
            recommendedClass = SoldierClass.OFFICER
        )
    )
}
