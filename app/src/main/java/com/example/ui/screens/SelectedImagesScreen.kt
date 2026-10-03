package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ImageItem
import com.example.ui.components.JKEmptyState
import com.example.ui.components.JKPrimaryButton
import com.example.ui.components.JKSecondaryButton
import com.example.ui.components.JKTopAppBar
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CopperDark
import com.example.ui.theme.CopperLight
import com.example.ui.theme.CopperPrimary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppViewModel

@Composable
fun SelectedImagesScreen(
    viewModel: AppViewModel,
    onBackClick: () -> Unit,
    onAddMoreClick: () -> Unit,
    onEditImageClick: (ImageItem) -> Unit,
    onNavigateToPdfSettings: () -> Unit
) {
    val selectedImages by viewModel.selectedImages.collectAsState()
    var itemToDelete by remember { mutableStateOf<ImageItem?>(null) }
    var addMenuExpanded by remember { mutableStateOf(false) }

    // Direct Gallery Picker from this screen
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addMultipleImages(uris.map { it.toString() })
        }
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = "Delete this page?",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove Page ${itemToDelete?.pageNumber} from this PDF?",
                    color = TextGrayLight,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { viewModel.removeImage(it.id) }
                        itemToDelete = null
                    },
                    modifier = Modifier.testTag("dialog_confirm_delete_button")
                ) {
                    Text(text = "Delete", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { itemToDelete = null },
                    modifier = Modifier.testTag("dialog_cancel_delete_button")
                ) {
                    Text(text = "Cancel", color = CopperLight)
                }
            },
            containerColor = CharcoalCardElevated,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Scaffold(
        topBar = {
            JKTopAppBar(
                title = "Selected Images for PDF",
                onBackClick = onBackClick,
                actions = {
                    if (selectedImages.isNotEmpty()) {
                        Box {
                            IconButton(
                                onClick = { addMenuExpanded = true },
                                modifier = Modifier.testTag("selected_images_add_more_topbar")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add More",
                                    tint = CopperLight
                                )
                            }
                            DropdownMenu(
                                expanded = addMenuExpanded,
                                onDismissRequest = { addMenuExpanded = false },
                                modifier = Modifier.background(CharcoalCard)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Take Photo", color = TextWhite) },
                                    leadingIcon = { Icon(Icons.Default.CameraAlt, null, tint = CopperLight) },
                                    onClick = {
                                        addMenuExpanded = false
                                        onAddMoreClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("From Gallery", color = TextWhite) },
                                    leadingIcon = { Icon(Icons.Default.PhotoLibrary, null, tint = CopperLight) },
                                    onClick = {
                                        addMenuExpanded = false
                                        galleryPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (selectedImages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CharcoalBlack)
                        .border(BorderStroke(0.5.dp, CharcoalBorder))
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        JKSecondaryButton(
                            text = "+ Add More",
                            icon = Icons.Default.AddPhotoAlternate,
                            onClick = { addMenuExpanded = true },
                            modifier = Modifier.weight(0.44f),
                            testTag = "selected_images_add_more_button"
                        )

                        JKPrimaryButton(
                            text = "PDF Settings (${selectedImages.size})",
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onNavigateToPdfSettings,
                            modifier = Modifier.weight(0.56f),
                            testTag = "selected_images_proceed_settings_button"
                        )
                    }
                }
            }
        },
        containerColor = CharcoalBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("selected_images_screen")
        ) {
            if (selectedImages.isEmpty()) {
                // Clear Empty State when there are no images
                JKEmptyState(
                    icon = Icons.Default.Image,
                    title = "No images selected",
                    subtitle = "Capture documents with the scanner or select photos from your gallery to compile into a PDF.",
                    actionButtonText = "Add Images",
                    onActionClick = onAddMoreClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(selectedImages, key = { _, item -> item.id }) { index, item ->
                        ImageThumbnailCard(
                            item = item,
                            index = index,
                            totalCount = selectedImages.size,
                            onMoveLeft = { if (index > 0) viewModel.moveImage(index, index - 1) },
                            onMoveRight = { if (index < selectedImages.size - 1) viewModel.moveImage(index, index + 1) },
                            onRotate = { viewModel.rotateImage(item.id) },
                            onDelete = { itemToDelete = item },
                            onEdit = {
                                viewModel.setActiveEditorImage(item)
                                onEditImageClick(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnailCard(
    item: ImageItem,
    index: Int,
    totalCount: Int,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCardElevated),
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("image_thumbnail_card_${item.pageNumber}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Reordering Controls Bar: Move Earlier / Move Later
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Text(
                    text = "Page ${item.pageNumber}",
                    color = CopperLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                // Reorder controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveLeft,
                        enabled = index > 0,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Move Earlier",
                            tint = if (index > 0) CopperLight else TextGrayMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveRight,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Move Later",
                            tint = if (index < totalCount - 1) CopperLight else TextGrayMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Thumbnail Preview Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalCard)
                    .border(1.dp, CharcoalBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                if (item.uri != null) {
                    // Real image thumbnail with Coil
                    AsyncImage(
                        model = Uri.parse(item.uri),
                        contentDescription = "Page ${item.pageNumber}",
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(item.rotationDegrees.toFloat()),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Document Canvas placeholder
                    Column(
                        modifier = Modifier
                            .fillMaxSize(0.78f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CharcoalBlack)
                            .border(1.dp, CopperPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .rotate(item.rotationDegrees.toFloat())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DOC",
                                color = CopperLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = TextGrayLight,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(modifier = Modifier.fillMaxWidth(0.9f).height(4.dp).background(CharcoalBorder))
                            Box(modifier = Modifier.fillMaxWidth(0.7f).height(4.dp).background(CharcoalBorder))
                            Box(modifier = Modifier.fillMaxWidth(0.8f).height(4.dp).background(CharcoalBorder))
                        }

                        Text(
                            text = item.filterType.displayName,
                            color = TextGrayLight,
                            fontSize = 8.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Edit Overlay pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharcoalBlack.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = CopperLight,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Edit",
                            color = TextWhite,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Rotate & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rotate Button (real 90° increment)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalCard)
                        .clickable(onClick = onRotate)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("rotate_button_${item.pageNumber}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "Rotate",
                        tint = CopperLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.rotationDegrees}°",
                        color = CopperLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Delete Button with Confirmation trigger
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CharcoalCard)
                        .testTag("delete_button_${item.pageNumber}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Page",
                        tint = TextGrayLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
