package com.example.candbsee.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object CanIcons {
    private fun vectorIcon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = addPathNodes(pathData),
                fill = SolidColor(Color.White)
            )
        }.build()

    val Folder: ImageVector by lazy {
        vectorIcon("Folder", "M10,4H4C2.89,4 2,4.89 2,6V18A2,2 0 0,0 4,20H20A2,2 0 0,0 22,18V8C22,6.89 21.1,6 20,6H12L10,4Z")
    }

    val Save: ImageVector by lazy {
        vectorIcon("Save", "M15,9H5V5H15M12,19A3,3 0 0,1 9,16A3,3 0 0,1 12,13A3,3 0 0,1 15,16A3,3 0 0,1 12,19M17,3H5C3.89,3 3,3.9 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V7L17,3Z")
    }

    val Overview: ImageVector by lazy {
        vectorIcon("Overview", "M13,3V9H21V3M13,21H21V11H13M3,21H11V15H3M3,13H11V3H3V13Z")
    }

    val Node: ImageVector by lazy {
        vectorIcon("Node", "M6,4H18V5H20V7H18V9H20V11H18V13H20V15H18V17H20V19H18V20H6V19H4V17H6V15H4V13H6V11H4V9H6V7H4V5H6V4M8,6V18H16V6H8ZM10,8H14V16H10V8Z")
    }

    val Message: ImageVector by lazy {
        vectorIcon("Message", "M20 4H4C2.9 4 2.01 4.9 2.01 6L2 18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM20 8L12 13L4 8V6L12 11L20 6V8Z")
    }

    val Signal: ImageVector by lazy {
        vectorIcon("Signal", "M16,6L18.29,8.29L13.41,13.17L9.41,9.17L2,16.59L3.41,18L9.41,12L13.41,16L19.71,9.71L22,12V6H16Z")
    }

    val Raw: ImageVector by lazy {
        vectorIcon("Raw", "M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20M8,12V14H16V12H8M8,16V18H13V16H8Z")
    }

    val Add: ImageVector by lazy {
        vectorIcon("Add", "M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z")
    }

    val Edit: ImageVector by lazy {
        vectorIcon("Edit", "M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z")
    }

    val Delete: ImageVector by lazy {
        vectorIcon("Delete", "M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z")
    }

    val Search: ImageVector by lazy {
        vectorIcon("Search", "M9.5,3A6.5,6.5 0 0,1 16,9.5C16,11.11 15.41,12.59 14.44,13.73L14.71,14H15.5L20.5,19L19,20.5L14,15.5V14.71L13.73,14.44C12.59,15.41 11.11,16 9.5,16A6.5,6.5 0 0,1 3,9.5A6.5,6.5 0 0,1 9.5,3M9.5,5C7,5 5,7 5,9.5C5,12 7,14 9.5,14C12,14 14,12 14,9.5C14,7 12,5 9.5,5Z")
    }

    val Close: ImageVector by lazy {
        vectorIcon("Close", "M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z")
    }

    val ArrowRight: ImageVector by lazy {
        vectorIcon("ArrowRight", "M8.59,16.58L13.17,12L8.59,7.41L10,6L16,12L10,18L8.59,16.58Z")
    }

    val ArrowDown: ImageVector by lazy {
        vectorIcon("ArrowDown", "M7.41,8.58L12,13.17L16.59,8.58L18,10L12,16L6,10L7.41,8.58Z")
    }

    val Menu: ImageVector by lazy {
        vectorIcon("Menu", "M3,6H21V8H3V6M3,11H21V13H3V11M3,16H21V18H3V16Z")
    }

    val ArrowLeft: ImageVector by lazy {
        vectorIcon("ArrowLeft", "M15.41,16.58L10.83,12L15.41,7.41L14,6L8,12L14,18L15.41,16.58Z")
    }
}
