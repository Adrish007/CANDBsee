package com.example.candbsee

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.candbsee.file.openDbcFileDialog
import com.example.candbsee.file.saveDbcFile
import com.example.candbsee.model.*
import com.example.candbsee.parser.DbcParser
import com.example.candbsee.sample.SampleDbc
import com.example.candbsee.ui.*

@Composable
fun App() {
    MaterialTheme(colorScheme = CanDbTheme.colorScheme) {
        var network by remember { mutableStateOf(SampleDbc.network) }
        var currentTab by remember { mutableStateOf(NavigationTab.OVERVIEW) }
        
        var selectedNodeName by remember { mutableStateOf<String?>(network.nodes.firstOrNull()?.name) }
        var selectedMessageId by remember { mutableStateOf<Long?>(network.messages.firstOrNull()?.id) }
        
        var showNewNodeDialog by remember { mutableStateOf(false) }
        var showNewMessageDialog by remember { mutableStateOf(false) }
        
        var isSidebarExpanded by remember { mutableStateOf(true) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CanDbTheme.BgDark
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Navigation Sidebar
                NavSidebar(
                    isExpanded = isSidebarExpanded,
                    onToggleExpand = { isSidebarExpanded = !isSidebarExpanded },
                    selectedTab = currentTab,
                    nodeCount = network.nodes.size,
                    msgCount = network.messages.size,
                    sigCount = network.allSignals.size,
                    onSelectTab = { currentTab = it },
                    onOpenFile = {
                        openDbcFileDialog { fileName, content ->
                            try {
                                val loaded = DbcParser.parse(fileName, content)
                                network = loaded
                                selectedNodeName = loaded.nodes.firstOrNull()?.name
                                selectedMessageId = loaded.messages.firstOrNull()?.id
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onSaveFile = {
                        val content = DbcParser.serialize(network)
                        saveDbcFile(network.fileName, content)
                    },
                    onNewNode = { showNewNodeDialog = true },
                    onNewMessage = { showNewMessageDialog = true }
                )

                // Main Content Screen
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentTab) {
                        NavigationTab.OVERVIEW -> {
                            NetworkOverviewScreen(
                                network = network,
                                onNavigateToNodes = { currentTab = NavigationTab.NODES },
                                onNavigateToMessages = { currentTab = NavigationTab.MESSAGES },
                                onNavigateToSignals = { currentTab = NavigationTab.SIGNALS },
                                onSelectNode = { nodeName ->
                                    selectedNodeName = nodeName
                                    currentTab = NavigationTab.NODES
                                }
                            )
                        }
                        NavigationTab.NODES -> {
                            NodesScreen(
                                network = network,
                                selectedNodeName = selectedNodeName,
                                onSelectNode = { selectedNodeName = it },
                                onNewNode = { showNewNodeDialog = true }
                            )
                        }
                        NavigationTab.MESSAGES -> {
                            MessagesScreen(
                                network = network,
                                selectedMessageId = selectedMessageId,
                                onSelectMessage = { selectedMessageId = it },
                                onNewMessage = { showNewMessageDialog = true },
                                onUpdateMessage = { updatedMsg ->
                                    val updatedList = network.messages.map { if (it.id == updatedMsg.id) updatedMsg else it }
                                    network = network.copy(messages = updatedList)
                                }
                            )
                        }
                        NavigationTab.SIGNALS -> {
                            SignalsScreen(
                                network = network,
                                onNavigateToMessage = { msgId ->
                                    selectedMessageId = msgId
                                    currentTab = NavigationTab.MESSAGES
                                }
                            )
                        }
                        NavigationTab.RAW -> {
                            RawEditorScreen(
                                network = network,
                                onApplyNewText = { newText ->
                                    try {
                                        val reparsed = DbcParser.parse(network.fileName, newText)
                                        network = reparsed
                                        selectedNodeName = reparsed.nodes.firstOrNull()?.name
                                        selectedMessageId = reparsed.messages.firstOrNull()?.id
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showNewNodeDialog) {
            NewNodeDialog(
                attributeDefs = network.attributeDefinitions.values.filter { it.objectType == AttributeObjectType.NODE },
                onDismiss = { showNewNodeDialog = false },
                onSave = { newNode ->
                    network = network.copy(nodes = (network.nodes + newNode).distinctBy { it.name }.sortedBy { it.name })
                    selectedNodeName = newNode.name
                    showNewNodeDialog = false
                    currentTab = NavigationTab.NODES
                }
            )
        }

        if (showNewMessageDialog) {
            NewMessageDialog(
                allNodes = network.nodes.map { it.name },
                attributeDefs = network.attributeDefinitions.values.filter { it.objectType == AttributeObjectType.MESSAGE },
                onDismiss = { showNewMessageDialog = false },
                onSave = { newMsg ->
                    val newMsgs = (network.messages + newMsg).distinctBy { it.id }.sortedBy { it.cleanId }
                    network = network.copy(messages = newMsgs)
                    selectedMessageId = newMsg.id
                    showNewMessageDialog = false
                    currentTab = NavigationTab.MESSAGES
                }
            )
        }
    }
}
