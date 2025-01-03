package com.ta_da.android.enums

import androidx.annotation.DrawableRes
import com.ta_da.android.R

interface IconProvider {
    val iconId: Int
}

enum class GameCategory(@DrawableRes val iconId: Int, val items: List<Enum<*>>) {
    STATIONERY(R.drawable.game_stationery, Stationery.entries),
    PERSONAL_CARE(R.drawable.game_personal_care, PersonalCare.entries),
    TOYS(R.drawable.game_toys, Toys.entries),
    ACCESSORIES_AND_JEWELRY(R.drawable.game_accessories_and_jewelry, AccessoriesAndJewelry.entries),
    ELECTRONICS(R.drawable.game_electronics, Electronics.entries)
}

enum class Stationery(@DrawableRes override val iconId: Int) : IconProvider {
    NOTEBOOK(R.drawable.game_notebook),
    BALLPOINT_PEN(R.drawable.game_ballpoint_pen),
    RULER(R.drawable.game_ruler),
    ERASER(R.drawable.game_eraser),
    COMPASS(R.drawable.game_compass),
    PEN_CASE(R.drawable.game_pen_case)
}

enum class PersonalCare(@DrawableRes override val iconId: Int) : IconProvider {
    SOAP(R.drawable.game_soap),
    SHAMPOO(R.drawable.game_shampoo),
    SHAVING_RAZOR(R.drawable.game_shaving_razor),
    TOOTHBRUSH(R.drawable.game_toothbrush),
    TOWEL(R.drawable.game_towel),
    TOOTHPASTE(R.drawable.game_toothpaste)
}

enum class Toys(@DrawableRes override val iconId: Int) : IconProvider {
    WATER_GUN(R.drawable.game_water_gun),
    DOLL(R.drawable.game_doll),
    BALL(R.drawable.game_ball),
    TOY_ROBOT(R.drawable.game_toy_robot),
    TOY_CAR(R.drawable.game_toy_car),
    PLUSH_TOY(R.drawable.game_plush_toy)
}

enum class AccessoriesAndJewelry(@DrawableRes override val iconId: Int) : IconProvider {
    RING(R.drawable.game_ring),
    SUNGLASSES(R.drawable.game_sunglasses),
    WATCH(R.drawable.game_watch),
    EARRINGS(R.drawable.game_earrings),
    NECKLACE(R.drawable.game_necklace),
    HAIR_CLIP(R.drawable.game_hair_clip)
}

enum class Electronics(@DrawableRes override val iconId: Int) : IconProvider {
    HEADPHONES(R.drawable.game_headphones),
    GAME_PAD(R.drawable.game_game_pad),
    PHONE(R.drawable.game_phone),
    FLASHLIGHT(R.drawable.game_flashlight),
    MOUSE(R.drawable.game_mouse),
    LAPTOP(R.drawable.game_laptop)
}
