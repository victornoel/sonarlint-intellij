/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015-2021 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.core.server.events

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import org.sonarlint.intellij.common.util.SonarLintUtils.getService
import org.sonarlint.intellij.config.global.ServerConnection
import org.sonarlint.intellij.core.AnalysisEnv
import org.sonarlint.intellij.core.ProjectBindingManager
import org.sonarlint.intellij.core.ConnectedAnalysisEnv
import java.util.Optional

interface ServerEventsService {
    fun autoSubscribe(analysisEnv: AnalysisEnv)
    fun unsubscribe(project: Project)
}

class ServerEventsProductionService : ServerEventsService {
    override fun autoSubscribe(analysisEnv: AnalysisEnv) {
        if (analysisEnv is ConnectedAnalysisEnv) {
            analysisEnv.engineIfStarted?.subscribeForEvents(
                analysisEnv.connection.endpointParams,
                analysisEnv.connection.httpClient,
                getActiveProjectKeys(analysisEnv.connection)
            )
        }
    }

    override fun unsubscribe(project: Project) {
        val bindingManager = bindingManager(project)
        val projectConnection = bindingManager.tryGetServerConnection().orElse(null)
        projectConnection?.let { connection ->
            bindingManager.validConnectedEngine?.subscribeForEvents(
                connection.endpointParams,
                connection.httpClient,
                getActiveProjectKeys(connection).minus(bindingManager.uniqueProjectKeys)
            )
        }
    }

    private fun getActiveProjectKeys(serverConnection: ServerConnection) =
        ProjectManager.getInstance().openProjects.toList()
            .filter { bindingManager(it).tryGetServerConnection().equals(Optional.of(serverConnection)) }
            .flatMap { bindingManager(it).uniqueProjectKeys }
            .toSet()

    private fun bindingManager(project: Project): ProjectBindingManager =
        getService(project, ProjectBindingManager::class.java)
}
