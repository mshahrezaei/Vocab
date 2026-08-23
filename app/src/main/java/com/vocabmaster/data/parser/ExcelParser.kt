package com.vocabmaster.data.parser

import android.content.Context
import android.net.Uri
import com.vocabmaster.data.db.entities.Lesson
import com.vocabmaster.data.db.entities.Word
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

data class ParsedLesson(
    val lesson: Lesson,
    val words: List<Word>
)

class ExcelParser(private val context: Context) {

    fun parse(uri: Uri): List<ParsedLesson> {
        val results = mutableListOf<ParsedLesson>()
        val fileName = getFileName(uri) ?: "Unknown"

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook: Workbook = XSSFWorkbook(inputStream)

            for (sheetIndex in 0 until workbook.numberOfSheets) {
                val sheet = workbook.getSheetAt(sheetIndex)
                val parsed = parseSheet(sheet, fileName)
                if (parsed != null) results.add(parsed)
            }
            workbook.close()
        }
        return results
    }

    private fun parseSheet(sheet: Sheet, fileName: String): ParsedLesson? {
        if (sheet.physicalNumberOfRows < 2) return null

        val headerRow = sheet.getRow(0) ?: return null
        val headers = mutableMapOf<String, Int>()

        for (i in 0 until headerRow.physicalNumberOfCells) {
            val cell = headerRow.getCell(i) ?: continue
            val name = getCellString(cell).lowercase().trim()
            headers[name] = i
        }

        val type = detectSheetType(headers) ?: return null
        val words = mutableListOf<Word>()

        for (rowIndex in 1 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIndex) ?: continue
            val word = parseRow(row, headers, type) ?: continue
            words.add(word)
        }

        if (words.isEmpty()) return null

        val lesson = Lesson(
            name = sheet.sheetName,
            sourceFile = fileName,
            type = type,
            totalWords = words.size
        )

        return ParsedLesson(lesson, words)
    }

    private fun detectSheetType(headers: Map<String, Int>): String? {
        val keys = headers.keys
        return when {
            keys.any { it.contains("collocation") } -> "collocation"
            keys.any { it.contains("phrasal") } -> "phrasal"
            keys.any { it.contains("chunk") || it.contains("دسته") } -> "chunk"
            keys.any { it.contains("wbs") || it.contains("ows") || it.contains("item") } -> "ows"
            else -> null
        }
    }

    private fun parseRow(row: Row, headers: Map<String, Int>, type: String): Word? {
        val term = when (type) {
            "collocation" -> getCellByHeaders(row, headers, listOf("collocation"))
            "phrasal" -> getCellByHeaders(row, headers, listOf("phrasal verb"))
            "chunk" -> getCellByHeaders(row, headers, listOf("chunk"))
            "ows" -> getCellByHeaders(row, headers, listOf("item / expression", "item", "expression"))
            else -> null
        } ?: return null

        if (term.isBlank()) return null

        val meaning = getCellByHeaders(row, headers, listOf("persian meaning", "meaning")) ?: ""
        val example1 = getCellByHeaders(row, headers, listOf("example 1")) ?: ""
        val example2 = getCellByHeaders(row, headers, listOf("example 2")) ?: ""
        val level = getCellByHeaders(row, headers, listOf("level", "cefr level")) ?: ""
        val topic = getCellByHeaders(row, headers, listOf("topic", "category", "دسته")) ?: ""
        val priority = getCellByHeaders(row, headers, listOf("priority rank"))?.toIntOrNull() ?: 0

        return Word(
            lessonId = 0,
            term = term.trim(),
            meaning = meaning.trim(),
            example1 = example1.trim(),
            example2 = example2.trim(),
            level = level,
            topic = topic,
            priorityRank = priority
        )
    }

    private fun getCellByHeaders(row: Row, headers: Map<String, Int>, candidates: List<String>): String? {
        for (candidate in candidates) {
            val entry = headers.entries.find { it.key.contains(candidate) } ?: continue
            val cell = row.getCell(entry.value) ?: continue
            val value = getCellString(cell)
            if (value.isNotBlank()) return value
        }
        return null
    }

    private fun getCellString(cell: Cell): String {
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toLong().toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }

    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }
}
