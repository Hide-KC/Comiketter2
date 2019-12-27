package com.kc.comiketter2.model.data

import com.kc.comiketter2.domain.usecase.EditUsersUseCase
import com.kc.comiketter2.domain.usecase.SearchUsersUseCase

interface AppDataSource : SearchUsersUseCase, EditUsersUseCase