package com.buylog.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.buylog.ClipboardLinkHandler
import com.buylog.data.model.Product
import com.buylog.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val showProductCard by viewModel.showProductCard.collectAsState()
    val parsedProduct by viewModel.parsedProduct.collectAsState()
    val isParsing by viewModel.isParsing.collectAsState()

    var manualUrl by remember { mutableStateOf("") }

    // 挂载剪贴板处理器
    ClipboardLinkHandler(viewModel = viewModel)

    // 动态计算间距实现垂直居中切换
    val topPadding by animateDpAsState(
        targetValue = if (showProductCard) 0.dp else 120.dp,
        animationSpec = tween(durationMillis = 600)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // 增加状态栏避让，防止内容顶到状态栏下面
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topPadding))

            // 顶部标题（解析后隐藏）
            AnimatedVisibility(visible = !showProductCard) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "BuyLog", 
                        style = MaterialTheme.typography.displaySmall, 
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "简单记录你的每一次购物", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // --- 动态输入框 (富文本样式切换) ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(if (showProductCard) 16.dp else 28.dp),
                color = if (showProductCard) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                        else MaterialTheme.colorScheme.surface,
                tonalElevation = if (showProductCard) 0.dp else 2.dp,
                shadowElevation = if (showProductCard) 0.dp else 4.dp,
                border = if (showProductCard) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(if (showProductCard) 4.dp else 16.dp)) {
                    TextField(
                        value = manualUrl,
                        onValueChange = { manualUrl = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (showProductCard) 48.dp else 150.dp),
                        placeholder = { 
                            Text(
                                if (showProductCard) "粘贴新链接..." else "粘贴淘宝/京东商品链接或口令于此...",
                                style = if (showProductCard) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                            ) 
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        textStyle = if (showProductCard) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                        singleLine = showProductCard,
                        maxLines = if (showProductCard) 1 else 10,
                        trailingIcon = if (showProductCard && manualUrl.isNotBlank()) {
                            {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { manualUrl = "" }) {
                                        Icon(Icons.Default.Clear, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                    IconButton(onClick = { 
                                        viewModel.parseUrl(manualUrl)
                                        manualUrl = ""
                                    }) {
                                        Icon(Icons.Default.Search, "Parse", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        } else null
                    )

                    // 解析前，按钮另起一行显示
                    if (!showProductCard && manualUrl.isNotBlank()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { manualUrl = "" },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("清空输入")
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { 
                                    viewModel.parseUrl(manualUrl)
                                    manualUrl = "" 
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("立即解析")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 解析结果展示区 ---
            AnimatedVisibility(
                visible = showProductCard && parsedProduct != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                parsedProduct?.let { product ->
                    ProductEditSection(
                        product = product,
                        onCancel = { viewModel.closeProductCard() },
                        onSave = { editedProduct ->
                            viewModel.saveProduct(editedProduct)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        // 加载蒙层
        if (isParsing) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(strokeWidth = 4.dp)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("正在解析", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEditSection(
    product: Product,
    onCancel: () -> Unit,
    onSave: (Product) -> Unit
) {
    var title by remember(product) {
        mutableStateOf(product.title)
    }

    var price by remember(product) {
        mutableStateOf(product.price)
    }

    var selectedCategory by remember(product) {
        mutableStateOf(product.category)
    }

    var size by remember(product) {
        mutableStateOf(product.size)
    }

    val allImages = remember(product) {
        listOf(product.imageUrl) +
                if (product.images.isNotBlank()) {
                    product.images.split(",")
                } else {
                    emptyList()
                }
    }

    var selectedImageUrl by remember(product) {
        mutableStateOf(product.imageUrl)
    }

    val categories = listOf("上衣", "裤子", "鞋子", "配饰", "数码", "其他")

    val needsSize = selectedCategory in listOf("上衣", "裤子", "鞋子")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 1. 图片单选展示
            Text("选择封面图", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allImages) { url ->
                    val isSelected = selectedImageUrl == url
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                3.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedImageUrl = url }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2. 编辑字段
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("商品名称") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("到手价格") },
                    modifier = Modifier.weight(1f),
                    prefix = { Text("¥ ") },
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = product.platform,
                    onValueChange = {},
                    label = { Text("来源") },
                    modifier = Modifier.weight(0.7f),
                    readOnly = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 类别选择 (Chip 流)
            Text("商品类别", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 4. 尺码输入 (动态显示)
            AnimatedVisibility(visible = needsSize) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("输入规格 / 尺码") },
                        placeholder = { Text("例如: 白色 XL 或 42码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 5. 操作按钮
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onCancel, 
                    modifier = Modifier.height(56.dp).weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("取消", style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = {
                        val editedProduct = product.copy(
                            title = title,
                            price = price,
                            imageUrl = selectedImageUrl,
                            category = selectedCategory,
                            size = size
                        )
                        onSave(editedProduct)
                    },
                    modifier = Modifier.height(56.dp).weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("确认保存", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}
