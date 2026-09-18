package com.example.callsort.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.callsort.data.contactprovider.ContactsFetcher
import com.example.callsort.data.local.CallSortDatabase
import com.example.callsort.data.repository.ContactRepository
import com.example.callsort.domain.usecase.GetCategoryContactsUseCase
import com.example.callsort.domain.usecase.SyncContactsUseCase
import com.example.callsort.ui.screens.board.BoardScreen
import com.example.callsort.ui.screens.board.BoardViewModel
import com.example.callsort.ui.screens.category.CategoryManagementScreen
import com.example.callsort.ui.screens.category.CategoryViewModel
import com.example.callsort.ui.screens.contact.ContactDetailScreen
import com.example.callsort.ui.screens.contact.ContactViewModel

sealed class Screen(val route: String) {
    object Board : Screen("board")
    object CategoryManager : Screen("category_manager")
    object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: Long) = "contact_detail/$contactId"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // DI setup
    val database = CallSortDatabase.getDatabase(context)
    val contactsFetcher = ContactsFetcher(context)
    val repository = ContactRepository(database.contactDao(), database.categoryDao(), contactsFetcher)

    val getCategoryContactsUseCase = GetCategoryContactsUseCase(repository)
    val syncContactsUseCase = SyncContactsUseCase(repository)

    NavHost(
        navController = navController,
        startDestination = Screen.Board.route
    ) {
        composable(Screen.Board.route) {
            val viewModel: BoardViewModel = viewModel(
                factory = SimpleViewModelFactory {
                    BoardViewModel(repository, getCategoryContactsUseCase, syncContactsUseCase)
                }
            )
            BoardScreen(
                viewModel = viewModel,
                onNavigateToCategoryManager = { navController.navigate(Screen.CategoryManager.route) },
                onContactClick = { contactId ->
                    navController.navigate(Screen.ContactDetail.createRoute(contactId))
                }
            )
        }

        composable(Screen.CategoryManager.route) {
            val viewModel: CategoryViewModel = viewModel(
                factory = SimpleViewModelFactory { CategoryViewModel(repository) }
            )
            CategoryManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ContactDetail.route,
            arguments = listOf(navArgument("contactId") { type = NavType.LongType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getLong("contactId") ?: return@composable
            val viewModel: ContactViewModel = viewModel(
                factory = SimpleViewModelFactory { ContactViewModel(repository, contactId) }
            )
            ContactDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Simple Factory helper for viewModels
class SimpleViewModelFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}
