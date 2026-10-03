package com.aipose.camera.posematch.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aipose.camera.posematch.data.PoseItem
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import java.util.*
import com.aipose.camera.posematch.R
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.stringResource

internal val CategoryIcons: Map<String, ImageVector> = mapOf(
    "Viral" to Icons.Default.Whatshot,
    "Couple" to Icons.Default.Favorite,
    "Sunset" to Icons.Default.WbTwilight,
    "Dark" to Icons.Default.DarkMode,
    "Mirror" to Icons.Default.Flip,
    "Beach" to Icons.Default.BeachAccess,
    "Cafe" to Icons.Default.Coffee,
    "Family" to Icons.Default.FamilyRestroom,
    "Nature" to Icons.Default.Nature,
    "Waterfall" to Icons.Default.Water
)
internal val CategoryOrder = listOf(
    "Viral", "Couple", "Sunset", "Dark", "Mirror",
    "Beach", "Cafe", "Family", "Nature", "Waterfall"
)
internal fun iconForCategory(cat: String): ImageVector =
    CategoryIcons[cat] ?: Icons.Default.Collections
/** Blueprint browser: per-trend rows of three with "Show all" albums. */
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenCamera: () -> Unit,
    onLaunchGalleryPicker: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val allPoses by viewModel.defaultPoses.collectAsState()

    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    fun dismissKeyboard() {
        keyboard?.hide()
        focusManager.clearFocus()
    }

    var showAllCategory by remember { mutableStateOf<String?>(null) }

    fun openPose(pose: PoseItem) {
        dismissKeyboard()
        viewModel.setSearchQuery("")
        viewModel.selectPose(pose)
        onOpenCamera()
    }

    fun openCategory(cat: String) {
        dismissKeyboard()
        showAllCategory = cat
    }

    // Reset search whenever Home leaves composition (camera, tab switch, picker) so
    // returning never shows stale text or a stuck results view.
    DisposableEffect(Unit) {
        onDispose { viewModel.setSearchQuery("") }
    }

    val searchResults = remember(allPoses, query) {
        if (query.isBlank()) emptyList()
        else allPoses.filter { pose ->
            pose.title.contains(query, true) ||
                    pose.description.contains(query, true) ||
                    pose.category.contains(query, true) ||
                    pose.tags.any { it.contains(query, true) }
        }
    }

    // Trends in a stable, pleasant order, only those that actually have blueprints.
    val orderedCategories = remember(allPoses) {
        val present = allPoses.map { it.category }.toSet()
        (CategoryOrder.filter { it in present } + present.filter { it !in CategoryOrder })
    }
    val grouped = remember(allPoses) { allPoses.groupBy { it.category } }
    // Featured pose for the hero card (prefer a couple pose, else the first available).
    val heroPose = remember(allPoses) {
        allPoses.firstOrNull { it.category == "Couple" } ?: allPoses.firstOrNull()
    }
    val heroImageAsset = remember(heroPose) { heroPose?.image?.let(::assetPathOf) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp)
        ) {
            // Compact top bar — brand + camera & gallery quick actions (no profile).
            item(key = "home_topbar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    // Right end — gallery only (camera now lives in the hero card below).
                    HomeActionIcon(
                        icon = Icons.Default.AddPhotoAlternate,
                        contentDescription = stringResource(R.string.action_import),
                        onClick = { dismissKeyboard(); onLaunchGalleryPicker() },
                        tag = "home_gallery_icon"
                    )
                }
            }

            // Search
            item(key = "home_search") {
                TextField(
                    value = query,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            stringResource(R.string.home_search_hint),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.action_search),
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery(""); dismissKeyboard() }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("home_search_bar"),
                    // Filled TextField: kill the bottom indicator ("blue line") in every state.
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SoftCardGray,
                        unfocusedContainerColor = SoftCardGray,
                        disabledContainerColor = SoftCardGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = AccentCopper,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { dismissKeyboard() })
                )
            }

            if (query.isNotBlank()) {
                // Search results — flat 3-column grid.
                if (searchResults.isEmpty()) {
                    item(key = "search_empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(SoftCardGray, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.home_no_blueprints),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    searchResults.chunked(3).forEachIndexed { idx, rowPoses ->
                        item(key = "search_row_$idx") {
                            PoseThumbRow(rowPoses) { openPose(it) }
                        }
                    }
                }
            } else {
                // Hero card — loads a featured pose and opens the live camera guide.
                item(key = "home_hero") {
                    PoseHeroCard(
                        imageAssetPath = heroImageAsset,
                        onStartPosing = {
                            dismissKeyboard()
                            heroPose?.let { viewModel.selectPose(it) }
                            onOpenCamera()
                        }
                    )
                }
                // Guidance caption so first-timers know the core interaction.
                item(key = "home_hint") {
                    Text(
                        text = stringResource(R.string.home_tap_hint),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                // Per-trend sections: name + "Show all", first 3 blueprints.
                orderedCategories.forEach { category ->
                    val poses = grouped[category].orEmpty()
                    if (poses.isNotEmpty()) {
                        item(key = "cat_header_$category") {
                            CategoryHeader(
                                category = category,
                                showAll = poses.size > 3,
                                onShowAll = { openCategory(category) }
                            )
                        }
                        item(key = "cat_row_$category") {
                            PoseThumbRow(poses.take(3)) { openPose(it) }
                        }
                    }
                }
            }
        }

        // "Show all" — full album for one trend, same tap-to-camera flow.
        AnimatedVisibility(
            visible = showAllCategory != null,
            enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(250))
        ) {
            val cat = showAllCategory
            if (cat != null) {
                CategoryAlbumScreen(
                    category = cat,
                    poses = grouped[cat].orEmpty(),
                    onBack = { showAllCategory = null },
                    onPoseClick = { openPose(it) }
                )
            }
        }

        BackHandler(enabled = showAllCategory != null) { showAllCategory = null }
    }
}
// Compact rounded-square action button for the Home top bar.
@Composable
internal fun HomeActionIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    accent: Boolean = false,
    tag: String
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (accent) AccentCopper else SoftCardGray)
            .border(1.dp, if (accent) Color.Transparent else GlassWhite, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
// Trend section header — icon + name on the left, "Show all →" on the right.
@Composable
internal fun CategoryHeader(category: String, showAll: Boolean, onShowAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                iconForCategory(category),
                contentDescription = null,
                tint = AccentCopper,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(category, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        if (showAll) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onShowAll() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("show_all_$category")
            ) {
                Text(
                    stringResource(R.string.see_all),
                    color = AccentCopper,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AccentCopper,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
// Home hero banner — "Strike Your Perfect Pose" with a Start Posing call-to-action.
@Composable
internal fun PoseHeroCard(imageAssetPath: String?, onStartPosing: () -> Unit) {
    val accent = AccentCopper
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.72f))))
    ) {
        // Decorative pose photo on the right, blended into the gradient.
        if (imageAssetPath != null) {
            AssetImage(
                assetPath = imageAssetPath,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(150.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to accent,
                            0.55f to accent.copy(alpha = 0.85f),
                            1f to Color.Transparent
                        )
                    )
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp, end = 12.dp, top = 14.dp, bottom = 14.dp)
        ) {
            Text(
                stringResource(R.string.hero_line1),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.hero_line2),
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .clickable { onStartPosing() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("home_start_posing"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(R.string.hero_cta),
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
// A row of up to 3 blueprint thumbnails.
@Composable
internal fun PoseThumbRow(poses: List<PoseItem>, onClick: (PoseItem) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        poses.forEach { pose ->
            PoseThumb(pose = pose, modifier = Modifier.weight(1f), onClick = { onClick(pose) })
        }
        repeat(3 - poses.size) { Spacer(modifier = Modifier.weight(1f)) }
    }
}

// Compact blueprint thumbnail: image with title overlay.
@Composable
internal fun PoseThumb(pose: PoseItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF14141A))
            .border(0.5.dp, GlassWhite, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("pose_blueprint_${pose.id}")
    ) {
        val context = LocalContext.current
        if (pose.image.isNotEmpty()) {
            val assetPath = assetPathOf(pose.image)
            when {
                assetPath != null -> AssetImage(
                    assetPath = assetPath,
                    contentDescription = pose.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                pose.image.startsWith("http") || pose.image.contains("/") -> AsyncImage(
                    model = pose.image,
                    contentDescription = pose.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                else -> {
                    val drawableId =
                        context.resources.getIdentifier(pose.image, "drawable", context.packageName)
                    if (drawableId != 0) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = pose.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Title overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                pose.title,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
// Full album for one trend — same 3-column grid + tap-to-camera flow.
@Composable
internal fun CategoryAlbumScreen(
    category: String,
    poses: List<PoseItem>,
    onBack: () -> Unit,
    onPoseClick: (PoseItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackgroundGradient)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("category_album_back")) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            Text(category, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            poses.chunked(3).forEachIndexed { idx, rowPoses ->
                item(key = "cat_album_row_$idx") {
                    PoseThumbRow(rowPoses) { onPoseClick(it) }
                }
            }
        }
    }
}
