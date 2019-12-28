package com.kc.comiketter2.model.data

import com.kc.comiketter2.domain.usecase.data.EditUsersUseCase
import com.kc.comiketter2.domain.usecase.data.SearchUsersUseCase

interface AppDataSource : SearchUsersUseCase, EditUsersUseCase