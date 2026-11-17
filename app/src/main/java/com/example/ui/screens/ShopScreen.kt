package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game.data.PlayerProfileState
import com.example.game.model.GameCatalog
import com.example.game.model.ShopConsumable
import com.example.game.model.UniformStyle
import com.example.game.model.WeaponSpec
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
fun ShopScreen(
    profile: PlayerProfileState,
    noticeText: String?,
    onBuyWeapon: (WeaponSpec) -> Unit,
    onEquipWeapon: (WeaponSpec) -> Unit,
    onBuyUniform: (UniformStyle) -> Unit,
    onBuyConsumable: (ShopConsumable) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = ОРУЖИЕ, 1 = ФОРМА, 2 = ПРИПАСЫ
    val shopWeapons = remember { GameCatalog.weapons.values.filter { it.priceStars > 0 } }
    val shopUniforms = remember { UniformStyle.entries.filter { it.priceStars > 0 } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrenchBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        RankHeaderBar(title = "АРМЕЙСКИЙ МАГАЗИН", profile = profile)

        // Category tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ОРУЖИЕ", "УНИФОРМА", "ПРИПАСЫ").forEachIndexed { idx, label ->
                val active = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) BrassGold else SlatePanel)
                        .border(1.dp, if (active) BrassGold else SlateBorder, RoundedCornerShape(4.dp))
                        .testTag("shop_tab_$idx")
                        .clickable { selectedTab = idx }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) TrenchBlack else ParchmentIvory,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (!noticeText.isNullOrBlank()) {
            Surface(
                color = TacticalGreen.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, TacticalGreen),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                Text(
                    text = noticeText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentIvory,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    items(shopWeapons, key = { it.id }) { weapon ->
                        val owned = profile.unlockedItemIds.contains(weapon.id)
                        val equipped = profile.selectedWeaponId == weapon.id
                        val canAfford = profile.stars >= weapon.priceStars

                        Surface(
                            color = GunmetalDark,
                            border = BorderStroke(1.dp, if (equipped) BrassGold else SlateBorder),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = weapon.nameRu,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = ParchmentIvory,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = weapon.subtitleRu,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AshGrey
                                        )
                                    }
                                    PriceOrOwnedChip(
                                        owned = owned,
                                        equipped = equipped,
                                        priceStars = weapon.priceStars
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StatMeterRow("Урон", weapon.damage, CrimsonAlert, Modifier.weight(1f))
                                    StatMeterRow("Точность", weapon.accuracy, BrassGold, Modifier.weight(1f))
                                    StatMeterRow("Скорость", weapon.fireRate, HorizonBlue, Modifier.weight(1f))
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Ww1CommandButton(
                                    text = when {
                                        equipped -> "ЭКИПИРОВАНО"
                                        owned -> "ЭКИПИРОВАТЬ"
                                        canAfford -> "КУПИТЬ ЗА ${weapon.priceStars} ★"
                                        else -> "НУЖНО ${weapon.priceStars} ★"
                                    },
                                    icon = if (owned) Icons.Filled.CheckCircle else Icons.Filled.ShoppingCart,
                                    isPrimary = !owned && canAfford,
                                    onClick = {
                                        if (owned) onEquipWeapon(weapon) else onBuyWeapon(weapon)
                                    },
                                    testTag = "shop_buy_${weapon.id}",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                1 -> {
                    items(shopUniforms, key = { it.id }) { uniform ->
                        val owned = profile.unlockedItemIds.contains(uniform.id)
                        val equipped = profile.selectedUniformId == uniform.id
                        val canAfford = profile.stars >= uniform.priceStars

                        Surface(
                            color = GunmetalDark,
                            border = BorderStroke(1.dp, if (equipped) BrassGold else SlateBorder),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(uniform.coatColorHex))
                                            .border(1.dp, BrassGold, RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = uniform.titleRu,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = ParchmentIvory,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = uniform.descriptionRu,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AshGrey
                                        )
                                    }
                                    PriceOrOwnedChip(owned, equipped, uniform.priceStars)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Ww1CommandButton(
                                    text = when {
                                        equipped -> "НАДЕТО"
                                        owned -> "НАДЕТЬ ФОРМУ"
                                        canAfford -> "КУПИТЬ ЗА ${uniform.priceStars} ★"
                                        else -> "НУЖНО ${uniform.priceStars} ★"
                                    },
                                    isPrimary = !owned && canAfford,
                                    onClick = { onBuyUniform(uniform) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                else -> {
                    items(ShopConsumable.entries, key = { it.id }) { item ->
                        val count = when (item) {
                            ShopConsumable.MEDKIT -> profile.medkitCount
                            ShopConsumable.ARMOR_PLATE -> profile.armorPlateCount
                            ShopConsumable.ORDNANCE_PACK -> profile.ordnancePackCount
                        }
                        val canAfford = profile.stars >= item.priceStars

                        Surface(
                            color = GunmetalDark,
                            border = BorderStroke(1.dp, SlateBorder),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalHospital,
                                            contentDescription = null,
                                            tint = BrassGold,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.titleRu,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = ParchmentIvory,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.descriptionRu,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = AshGrey
                                            )
                                        }
                                    }
                                    Text(
                                        text = "В запасе: $count",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = HorizonBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Ww1CommandButton(
                                    text = if (canAfford) "КУПИТЬ (+1) ЗА ${item.priceStars} ★" else "НУЖНО ${item.priceStars} ★",
                                    icon = Icons.Filled.ShoppingCart,
                                    isPrimary = canAfford,
                                    onClick = { onBuyConsumable(item) },
                                    testTag = "shop_buy_${item.id}",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(
            color = TrenchBlack,
            border = BorderStroke(1.dp, BrassMuted.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, SlateBorder, RoundedCornerShape(4.dp))
                        .testTag("shop_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = ParchmentIvory
                    )
                }
                Ww1CommandButton(
                    text = "ВЕРНУТЬСЯ В ШТАБ",
                    isPrimary = true,
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PriceOrOwnedChip(owned: Boolean, equipped: Boolean, priceStars: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                if (equipped) TacticalGreen.copy(alpha = 0.25f) else SlatePanel,
                RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                if (equipped) TacticalGreen else BrassMuted.copy(alpha = 0.6f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (owned) {
            Text(
                text = if (equipped) "В РУКАХ" else "КУПЛЕНО",
                style = MaterialTheme.typography.labelSmall,
                color = if (equipped) TacticalGreen else ParchmentIvory
            )
        } else {
            Text(
                text = "$priceStars",
                style = MaterialTheme.typography.labelLarge,
                color = BrassGold
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(Icons.Filled.Star, contentDescription = null, tint = BrassGold, modifier = Modifier.size(14.dp))
        }
    }
}
