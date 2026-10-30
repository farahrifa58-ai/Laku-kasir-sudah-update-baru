package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class TransactionItem(
    val productId: Long,
    val productName: String,
    val category: String,
    val unit: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val quantity: Double,
    val subtotal: Double = sellingPrice * quantity
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("productId", productId)
        obj.put("productName", productName)
        obj.put("category", category)
        obj.put("unit", unit)
        obj.put("costPrice", costPrice)
        obj.put("sellingPrice", sellingPrice)
        obj.put("quantity", quantity)
        obj.put("subtotal", subtotal)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): TransactionItem {
            return TransactionItem(
                productId = obj.optLong("productId", 0L),
                productName = obj.optString("productName", ""),
                category = obj.optString("category", ""),
                unit = obj.optString("unit", "pcs"),
                costPrice = obj.optDouble("costPrice", 0.0),
                sellingPrice = obj.optDouble("sellingPrice", 0.0),
                quantity = obj.optDouble("quantity", 1.0),
                subtotal = obj.optDouble("subtotal", 0.0)
            )
        }

        fun listToJsonString(list: List<TransactionItem>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJsonObject()) }
            return arr.toString()
        }

        fun listFromJsonString(jsonStr: String): List<TransactionItem> {
            val list = mutableListOf<TransactionItem>()
            if (jsonStr.isBlank()) return list
            try {
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(fromJsonObject(obj))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}
