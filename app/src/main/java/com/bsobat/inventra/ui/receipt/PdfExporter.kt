package com.bsobat.inventra.ui.receipt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.bsobat.inventra.R
import com.bsobat.inventra.data.model.Sale
import com.bsobat.inventra.ui.ext.formatPricePerUom
import com.bsobat.inventra.ui.ext.toFormattedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri

class PdfExporter(private val context: Context, val pdfDocument: PdfDocument = PdfDocument()) {

    private val pageWidth = 595
    private val pageHeight = 842
    private val leftMargin = 50f
    private val rightMargin = 545f
    private val topMargin = 50f
    private val bottomMargin = 50f
    private val maxContentHeight = pageHeight - topMargin - bottomMargin

    suspend fun exportReceiptToPdf(
        sale: Sale,
        configData: ConfigData?
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            var pageNumber = 1
            var currentPage = startNewPage(pdfDocument, pageNumber)
            var canvas = currentPage.canvas

            val paint = Paint().apply {
                textSize = 12f
                isAntiAlias = true
            }

            val titlePaint = Paint().apply {
                textSize = 20f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                textSize = 14f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val boldPaint = Paint().apply {
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }

            var yPosition = topMargin
            val centerX = (leftMargin + rightMargin) / 2

            // Helper function to check and create new page if needed
            fun checkPageBreak(requiredSpace: Float): Canvas {
                if (yPosition + requiredSpace > maxContentHeight) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    currentPage = startNewPage(pdfDocument, pageNumber)
                    canvas = currentPage.canvas
                    yPosition = topMargin
                }
                return canvas
            }

            // Receipt Header
            canvas.drawText(context.getString(R.string.receipt), leftMargin, yPosition, titlePaint)
            yPosition += 40f

            // Company Information Header
            configData?.companyName?.let { companyName ->
                canvas = checkPageBreak(30f)
                val textWidth = titlePaint.measureText(companyName)
                canvas.drawText(companyName, centerX - textWidth / 2, yPosition, titlePaint)
                yPosition += 10f
            }

            configData?.companyLogo?.let { logo ->
                canvas = checkPageBreak(220f) // Max logo height + padding
                val result = canvas.printLogo(logo, centerX, yPosition, paint, context)
                yPosition = result
            }

            configData?.companyDescription?.let { description ->
                canvas = checkPageBreak(20f)
                val textWidth = paint.measureText(description)
                canvas.drawText(description, centerX - textWidth / 2, yPosition, paint)
                yPosition += 20f
            }

            configData?.eventName?.let { eventName ->
                canvas = checkPageBreak(25f)
                val textWidth = subtitlePaint.measureText(eventName)
                canvas.drawText(eventName, centerX - textWidth / 2, yPosition, subtitlePaint)
                yPosition += 25f
            }

            // Add spacing and divider if company info exists
            if (configData?.companyName != null || configData?.companyLogo != null ||
                configData?.companyDescription != null || configData?.eventName != null
            ) {
                canvas = checkPageBreak(30f)
                yPosition += 10f
                canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
                yPosition += 20f
            }

            // Sale Details
            canvas = checkPageBreak(80f)
            canvas.drawText(
                context.getString(R.string.date_time) + ": " + sale.timestamp.toFormattedDateTime(),
                leftMargin,
                yPosition,
                paint
            )
            yPosition += 20f
            canvas.drawText(
                context.getString(R.string.sale_id) + ": ${sale.saleId}",
                leftMargin,
                yPosition,
                paint
            )
            yPosition += 20f

            sale.eventId?.let {
                canvas = checkPageBreak(20f)
                canvas.drawText(
                    context.getString(R.string.event) + ": $it",
                    leftMargin,
                    yPosition,
                    paint
                )
                yPosition += 20f
            }

            canvas = checkPageBreak(40f)
            yPosition += 20f
            canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
            yPosition += 20f

            // Items Section
            canvas = checkPageBreak(25f)
            canvas.drawText(context.getString(R.string.items), leftMargin, yPosition, boldPaint)
            yPosition += 25f

            val subtotal = sale.items.sumOf { it.totalPrice }
            val totalTax = sale.items.sumOf { it.totalPrice * (it.taxPercentage / 100) }
            val total = subtotal + totalTax

            sale.items.forEach { item ->
                item.description?.let {
                    canvas = checkPageBreak(43f) // Space for description + item details
                    canvas.drawText(it, leftMargin, yPosition, paint)
                    yPosition += 18f
                }

                val itemDetails = "${item.quantity} × ${item.unitPrice.formatPricePerUom()}"
                canvas.drawText(itemDetails, leftMargin + 20f, yPosition, paint)
                canvas.drawText(
                    item.totalPrice.formatPricePerUom(),
                    rightMargin - 80f,
                    yPosition,
                    paint
                )
                yPosition += 25f
            }

            canvas = checkPageBreak(30f)
            yPosition += 10f
            canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
            yPosition += 20f

            // Totals
            canvas = checkPageBreak(60f)
            canvas.drawText(
                context.getString(R.string.subtotal) + ":",
                leftMargin,
                yPosition,
                paint
            )
            canvas.drawText(subtotal.formatPricePerUom(), rightMargin - 80f, yPosition, paint)
            yPosition += 20f

            if (totalTax > 0) {
                canvas = checkPageBreak(20f)
                canvas.drawText(context.getString(R.string.tax) + ":", leftMargin, yPosition, paint)
                canvas.drawText(totalTax.formatPricePerUom(), rightMargin - 80f, yPosition, paint)
                yPosition += 20f
            }

            canvas = checkPageBreak(50f)
            canvas.drawText(
                context.getString(R.string.total) + ":",
                leftMargin,
                yPosition,
                boldPaint
            )
            canvas.drawText(total.formatPricePerUom(), rightMargin - 80f, yPosition, boldPaint)
            yPosition += 30f

            canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
            yPosition += 20f

            // Payments Section
            canvas = checkPageBreak(25f)
            canvas.drawText(context.getString(R.string.payments), leftMargin, yPosition, boldPaint)
            yPosition += 25f

            sale.payments.forEach { payment ->
                canvas = checkPageBreak(20f)
                canvas.drawText(payment.method, leftMargin, yPosition, paint)
                canvas.drawText(
                    payment.amount.formatPricePerUom(),
                    rightMargin - 80f,
                    yPosition,
                    paint
                )
                yPosition += 20f
            }

            canvas = checkPageBreak(50f)
            yPosition += 30f
            val thankYouText = context.getString(R.string.thank_you_for_your_purchase)
            val thankYouWidth = paint.measureText(thankYouText)
            canvas.drawText(thankYouText, centerX - thankYouWidth / 2, yPosition, paint)

            pdfDocument.finishPage(currentPage)

            // Save PDF
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Receipt_${sale.saleId}_$timestamp.pdf"
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "Receipts"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun startNewPage(pdfDocument: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        return pdfDocument.startPage(pageInfo)
    }
}

private fun Canvas.printLogo(
    logoPath: String,
    centerX: Float,
    y: Float,
    paint: Paint,
    context: Context
): Float {
    var yPosition = y
    try {
        val bitmap = if (logoPath.startsWith("content://")) {
            context.contentResolver.openInputStream(logoPath.toUri())?.use { inputStream ->
                android.graphics.BitmapFactory.decodeStream(inputStream)
            }
        } else {
            android.graphics.BitmapFactory.decodeFile(logoPath)
        }

        bitmap?.let {
            val maxWidth = 200f
            val scale = maxWidth / it.width
            val scaledHeight = it.height * scale

            val left = centerX - maxWidth / 2
            val top = yPosition
            val right = left + maxWidth
            val bottom = top + scaledHeight

            this.drawBitmap(
                it,
                null,
                android.graphics.RectF(left, top, right, bottom),
                paint
            )

            yPosition += scaledHeight + 20f
            it.recycle()
        }
    } catch (_: Exception) {
        val textWidth = paint.measureText(logoPath)
        this.drawText(logoPath, centerX - textWidth / 2, yPosition, paint)
        yPosition += 20f
    }

    return yPosition
}