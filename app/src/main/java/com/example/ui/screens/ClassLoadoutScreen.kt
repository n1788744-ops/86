package com.example.ui.screens

import android.opengl.GLSurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.game.data.PlayerProfileState
import com.example.game.model.GameCatalog
import com.example.game.model.SoldierClass
import com.example.game.model.UniformStyle
import com.example.game.model.WeaponSpec
import com.example.game.render3d.ClassShowcaseRenderer
import com.example.ui.components.RankHeaderBar
import com.example.ui.components.StatMeterRow
import com.example.ui.components.Ww1CommandButton
import com.example.ui.theme.AshGrey
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassMuted
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.HorizonBlue
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlatePanel
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TrenchBlack

@Composable
fun ClassLoadoutScreen(
    profile: PlayerProfileState,
    onSaveLoadout: (SoldierClass, WeaponSpec, UniformStyle, String) -> Unit,
    onDeployBattle: (SoldierClass, WeaponSpec, UniformStyle, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedClass by remember(profile.selectedClassId) {
        mutableStateOf(SoldierClass.entries.find { it.id == profile.selectedClassId } ?: SoldierClass.RIFLEMAN)
    }
    var selectedWeapon by remember(profile.selectedWeaponId, selectedClass) {
        mutableStateOf(
            GameCatalog.weapons[profile.selectedWeaponId]
                ?.takeIf { it.id == selectedClass.defaultWeaponId || it.id == selectedClass.altWeaponId }
                ?: GameCatalog.weapons[selectedClass.defaultWeaponId]!!
        )
    }
    var selectedUniform by remember(profile.selectedUniformId) {
        mutableStateOf(UniformStyle.entries.find { it.id == profile.selectedUniformId } ?: selectedClass.defaultUniform)
    }
    var selectedPerkId by remember(profile.selectedPerkId) {
        mutableStateOf(profile.selectedPerkId)
    }
    var rightTab by remember { mutableIntStateOf(0) } // 0 = СНАРЯГА, 1 = ФОРМА
    var userYawOffset by remember { mutableFloatStateOf(0f) }

    val showcaseRenderer = remember { ClassShowcaseRenderer() }
    showcaseRenderer.soldierClass = selectedClass
    showcaseRenderer.weaponType = selectedWeapon.type
    showcaseRenderer.uniformStyle = selectedUniform
    showcaseRenderer.userYawOffset = userYawOffset

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrenchBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        RankHeaderBar(title = "СМЕНИТЬ КЛАСС", profile = profile)

        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val isWide = maxWidth >= 680.dp

            if (isWide) {
                // Classic 3-column tactical loadout layout matching Screenshot #1
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.32f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClassSelectorPanel(
                            selectedClass = selectedClass,
                            onSelectClass = { sc ->
                                selectedClass = sc
                                selectedWeapon = GameCatalog.weapons[sc.defaultWeaponId]!!
                                selectedUniform = sc.defaultUniform
                                onSaveLoadout(sc, selectedWeapon, selectedUniform, selectedPerkId)
                            }
                        )
                        PerksPanel(
                            selectedPerkId = selectedPerkId,
                            onSelectPerk = { pid ->
                                selectedPerkId = pid
                                onSaveLoadout(selectedClass, selectedWeapon, selectedUniform, pid)
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(0.36f)
                            .fillMaxHeight()
                    ) {
                        Soldier3DTurntableCard(
                            renderer = showcaseRenderer,
                            selectedClass = selectedClass,
                            onDragDelta = { dx -> userYawOffset = (userYawOffset + dx * 0.45f) % 360f },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.32f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        EquipmentAndUniformPanel(
                            selectedClass = selectedClass,
                            selectedWeapon = selectedWeapon,
                            selectedUniform = selectedUniform,
                            activeTab = rightTab,
                            onTabChange = { rightTab = it },
                            onSelectWeapon = { w ->
                                selectedWeapon = w
                                onSaveLoadout(selectedClass, w, selectedUniform, selectedPerkId)
                            },
                            onSelectUniform = { u ->
                                selectedUniform = u
                                onSaveLoadout(selectedClass, selectedWeapon, u, selectedPerkId)
                            }
                        )
                    }
                }
            } else {
                // Portrait-optimized layout: Interactive 3D Soldier Showcase on top, tabbed panels below
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Soldier3DTurntableCard(
                        renderer = showcaseRenderer,
                        selectedClass = selectedClass,
                        onDragDelta = { dx -> userYawOffset = (userYawOffset + dx * 0.45f) % 360f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(235.dp)
                    )

                    ClassSelectorPanel(
                        selectedClass = selectedClass,
                        onSelectClass = { sc ->
                            selectedClass = sc
                            selectedWeapon = GameCatalog.weapons[sc.defaultWeaponId]!!
                            selectedUniform = sc.defaultUniform
                            onSaveLoadout(sc, selectedWeapon, selectedUniform, selectedPerkId)
                        }
                    )

                    EquipmentAndUniformPanel(
                        selectedClass = selectedClass,
                        selectedWeapon = selectedWeapon,
                        selectedUniform = selectedUniform,
                        activeTab = rightTab,
                        onTabChange = { rightTab = it },
                        onSelectWeapon = { w ->
                            selectedWeapon = w
                            onSaveLoadout(selectedClass, w, selectedUniform, selectedPerkId)
                        },
                        onSelectUniform = { u ->
                            selectedUniform = u
                            onSaveLoadout(selectedClass, selectedWeapon, u, selectedPerkId)
                        }
                    )

                    PerksPanel(
                        selectedPerkId = selectedPerkId,
                        onSelectPerk = { pid ->
                            selectedPerkId = pid
                            onSaveLoadout(selectedClass, selectedWeapon, selectedUniform, pid)
                        }
                    )
                }
            }
        }

        // Bottom Action Bar
        Surface(
            color = TrenchBlack,
            border = BorderStroke(1.dp, BrassMuted.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, SlateBorder, RoundedCornerShape(4.dp))
                        .testTag("loadout_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Закрыть",
                        tint = ParchmentIvory
                    )
                }

                Ww1CommandButton(
                    text = "СОХРАНИТЬ И В БОЙ",
                    icon = Icons.Filled.PlayArrow,
                    isPrimary = true,
                    onClick = { onDeployBattle(selectedClass, selectedWeapon, selectedUniform, selectedPerkId) },
                    testTag = "loadout_deploy_button",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Soldier3DTurntableCard(
    renderer: ClassShowcaseRenderer,
    selectedClass: SoldierClass,
    onDragDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glView = remember(context) {
        GLSurfaceView(context).apply {
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    DisposableEffect(glView) {
        glView.onResume()
        onDispose {
            glView.onPause()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, BrassMuted.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    onDragDelta(dragAmount)
                }
            }
    ) {
        AndroidView(
            factory = { glView },
            modifier = Modifier.fillMaxSize()
        )

        // Subtle vignette & class name badge overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Transparent, TrenchBlack.copy(alpha = 0.8f))
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedClass.categoryRu,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrassGold
                )
                Text(
                    text = selectedClass.titleRu,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ParchmentIvory
                )
            }
            Text(
                text = "◄ 3D МОДЕЛЬ ►",
                style = MaterialTheme.typography.labelSmall,
                color = AshGrey
            )
        }
    }
}

@Composable
private fun ClassSelectorPanel(
    selectedClass: SoldierClass,
    onSelectClass: (SoldierClass) -> Unit
) {
    Surface(
        color = GunmetalDark.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "ВЫБЕРИТЕ КЛАСС",
                style = MaterialTheme.typography.titleMedium,
                color = BrassGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            SoldierClass.entries.forEach { sc ->
                val active = sc == selectedClass
                val icon = when (sc) {
                    SoldierClass.OFFICER -> Icons.Filled.MilitaryTech
                    SoldierClass.RIFLEMAN -> Icons.Filled.GpsFixed
                    SoldierClass.ASSAULT -> Icons.Filled.FlashOn
                    SoldierClass.MG_GUNNER -> Icons.Filled.Shield
                    SoldierClass.ENGINEER -> Icons.Filled.Build
                    SoldierClass.SNIPER -> Icons.Filled.CenterFocusStrong
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (active) BrassMuted.copy(alpha = 0.25f) else SlatePanel.copy(alpha = 0.55f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (active) BrassGold else Color.Transparent,
                            shape = RoundedCornerShape(3.dp)
                        )
                        .testTag("class_option_${sc.id}")
                        .clickable { onSelectClass(sc) }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = sc.titleRu,
                            tint = if (active) BrassGold else AshGrey,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sc.titleRu,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (active) ParchmentIvory else AshGrey,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (active) "ВЫБРАНО" else "ОЗ ${sc.maxHealth}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) BrassGold else AshGrey.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PerksPanel(
    selectedPerkId: String,
    onSelectPerk: (String) -> Unit
) {
    Surface(
        color = GunmetalDark.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "УМЕНИЯ",
                style = MaterialTheme.typography.titleMedium,
                color = BrassGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val chunked = GameCatalog.perks.chunked(3)
            chunked.forEach { rowPerks ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowPerks.forEach { perk ->
                        val active = perk.id == selectedPerkId
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (active) TacticalGreen.copy(alpha = 0.30f) else SlatePanel)
                                .border(1.dp, if (active) TacticalGreen else SlateBorder, RoundedCornerShape(3.dp))
                                .clickable { onSelectPerk(perk.id) }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (perk.id) {
                                    "field_medic" -> Icons.Filled.LocalHospital
                                    "hardened" -> Icons.Filled.Security
                                    else -> Icons.Filled.Star
                                },
                                contentDescription = perk.titleRu,
                                tint = if (active) BrassGold else AshGrey,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = perk.titleRu,
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentIvory,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            val activePerk = GameCatalog.perks.find { it.id == selectedPerkId }
            if (activePerk != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${activePerk.titleRu}: ${activePerk.effectRu}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HorizonBlue
                )
            }
        }
    }
}

@Composable
private fun EquipmentAndUniformPanel(
    selectedClass: SoldierClass,
    selectedWeapon: WeaponSpec,
    selectedUniform: UniformStyle,
    activeTab: Int,
    onTabChange: (Int) -> Unit,
    onSelectWeapon: (WeaponSpec) -> Unit,
    onSelectUniform: (UniformStyle) -> Unit
) {
    Surface(
        color = GunmetalDark.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // СНАРЯГА / ФОРМА tabs matching Screenshot #1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabTitles = listOf("СНАРЯГА", "ФОРМА")
                tabTitles.forEachIndexed { idx, label ->
                    val isSel = activeTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSel) TacticalGreen else SlatePanel)
                            .border(1.dp, if (isSel) BrassGold else SlateBorder, RoundedCornerShape(3.dp))
                            .clickable { onTabChange(idx) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = ParchmentIvory
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activeTab == 0) {
                Text(
                    text = selectedClass.roleSummaryRu,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AshGrey
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Weapon stats bars
                StatMeterRow("Урон", selectedWeapon.damage, CrimsonAlert)
                Spacer(modifier = Modifier.height(4.dp))
                StatMeterRow("Точность", selectedWeapon.accuracy, BrassGold)
                Spacer(modifier = Modifier.height(4.dp))
                StatMeterRow("Скорострельность", selectedWeapon.fireRate, HorizonBlue)
                Spacer(modifier = Modifier.height(4.dp))
                StatMeterRow("Дальность", selectedWeapon.range, TacticalGreen)

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ОСНОВНОЕ ОРУЖИЕ",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrassGold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val classWeapons = listOfNotNull(
                    GameCatalog.weapons[selectedClass.defaultWeaponId],
                    GameCatalog.weapons[selectedClass.altWeaponId]
                )
                classWeapons.forEach { w ->
                    val isW = w.id == selectedWeapon.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isW) BrassMuted.copy(alpha = 0.22f) else SlatePanel)
                            .border(1.dp, if (isW) BrassGold else SlateBorder, RoundedCornerShape(3.dp))
                            .clickable { onSelectWeapon(w) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = w.nameRu,
                                style = MaterialTheme.typography.titleMedium,
                                color = ParchmentIvory,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${w.subtitleRu} • ${w.magazineSize}/${w.reserveAmmo}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AshGrey
                            )
                        }
                        if (isW) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Выбрано оружие",
                                tint = BrassGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Спец. умение: ${selectedClass.specialAbilityRu} (перезарядка ${selectedClass.specialCooldownSec.toInt()}с)",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrassGold
                )
            } else {
                Text(
                    text = "ПОЛЕВАЯ УНИФОРМА (обновляет 3D-модель)",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrassGold
                )
                Spacer(modifier = Modifier.height(6.dp))

                UniformStyle.entries.forEach { u ->
                    val isU = u == selectedUniform
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isU) BrassMuted.copy(alpha = 0.22f) else SlatePanel)
                            .border(1.dp, if (isU) BrassGold else SlateBorder, RoundedCornerShape(3.dp))
                            .clickable { onSelectUniform(u) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(u.coatColorHex))
                                .border(1.dp, ParchmentIvory.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = u.titleRu,
                                style = MaterialTheme.typography.titleMedium,
                                color = ParchmentIvory,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = u.descriptionRu,
                                style = MaterialTheme.typography.labelSmall,
                                color = AshGrey
                            )
                        }
                        if (isU) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Выбрана форма",
                                tint = BrassGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
