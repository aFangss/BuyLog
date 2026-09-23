package com.buylog.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buylog.data.db.AppDatabase
import com.buylog.data.model.Product
import com.buylog.utils.getValidLinkFromClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient()
    private val productDao = AppDatabase.getDatabase(application).productDao()
    
    // 好单库 V3 配置
    private val appId = "20270127"
    private val appSecret = "fbc2cd0fd3975e9f849f52f8fa16ed54"
    private val apiMethod = "analyze.clipboard"

    // --- UI 状态控制 ---
    private val _showClipboardDialog = MutableStateFlow(false)
    val showClipboardDialog: StateFlow<Boolean> = _showClipboardDialog.asStateFlow()

    private val _detectedLink = MutableStateFlow("")
    val detectedLink: StateFlow<String> = _detectedLink.asStateFlow()

    // 存储解析出来的临时商品数据
    private val _parsedProduct = MutableStateFlow<Product?>(null)
    val parsedProduct: StateFlow<Product?> = _parsedProduct.asStateFlow()

    // 控制解析结果卡片的显示
    private val _showProductCard = MutableStateFlow(false)
    val showProductCard: StateFlow<Boolean> = _showProductCard.asStateFlow()

    // 正在解析的状态（转圈圈）
    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private var lastProcessedLink: String? = null

    fun checkClipboardOnResume(context: Context) {
        val currentLink = context.getValidLinkFromClipboard()
        if (currentLink != null && currentLink != lastProcessedLink) {
            _detectedLink.value = currentLink
            _showClipboardDialog.value = true
        }
    }

    fun dismissDialog() {
        _showClipboardDialog.value = false
        lastProcessedLink = _detectedLink.value
    }

    fun confirmParse() {
        _showClipboardDialog.value = false
        val linkToParse = _detectedLink.value
        lastProcessedLink = linkToParse
        parseUrl(linkToParse)
    }

    fun parseUrl(url: String) {
        if (url.isBlank()) return

        viewModelScope.launch {
            fetchProductInfo(url)
        }
    }

    fun closeProductCard() {
        _showProductCard.value = false
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productDao.insertProduct(product)
                withContext(Dispatchers.Main) {
                    closeProductCard()
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateSign(params: Map<String, String>, secret: String): String {
        val sortedParams = params.toSortedMap()
        val sb = StringBuilder()
        for ((key, value) in sortedParams) {
            sb.append(key).append(value)
        }
        sb.append(secret)
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(sb.toString().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02X".format(it) }
    }

    private suspend fun fetchProductInfo(content: String) {
        withContext(Dispatchers.IO) {
            _isParsing.value = true
            try {
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val params = mutableMapOf(
                    "method" to apiMethod,
                    "app_id" to appId,
                    "date" to date,
                    "content" to content
                )
                val sign = generateSign(params, appSecret)
                params["sign"] = sign

                val jsonBody = JSONObject()
                params.forEach { (k, v) -> jsonBody.put(k, v) }
                
                val body = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("https://v3.api.haodanku.com/rest")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val result = response.body?.string()
                    val jsonRes = JSONObject(result ?: "")
                    val code = jsonRes.optInt("code")

                    if (code == 200) {
                        val data = jsonRes.getJSONObject("data")
                        
                        val product = Product(
                            title = data.optString("item_title"),
                            imageUrl = data.optString("item_pic"),
                            productUrl = data.optString("item_url"),
                            platform = if (data.optInt("plat_type") == 1) "淘宝" else "京东",
                            price = data.optString("item_end_price"),
                            images = data.optString("images")
                        )
                        
                        // 更新 UI 状态
                        _parsedProduct.value = product
                        _showProductCard.value = true
                    } else {
                        Log.e(
                            TAG,
                            "接口返回失败，httpCode=${response.code}, code=$code, message=${jsonRes.optString("msg")}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "商品解析异常：${e.message}", e)
            } finally {
                _isParsing.value = false
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
