/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *    Stéphane Maldini (stephane.maldini@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.plugin.platform.ui

import org.slf4j.LoggerFactory

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.context.request.RequestContextHolder as RCH

import org.grails.plugin.platform.util.PluginUtils
import org.grails.plugin.platform.util.PropertyNamespacer

/**
 * Helper methods for common UI features
 */
class UiExtensions implements ApplicationContextAware {
    final log = LoggerFactory.getLogger(UiExtensions)

    static final String SESSION_WRAPPER_KEY = 'plugin.platformCore.plugin.session.wrapper';
    static final String FLASH_WRAPPER_KEY = 'plugin.platformCore.plugin.flash.wrapper';
    static final String REQUEST_WRAPPER_KEY = 'plugin.platformCore.plugin.request.wrapper';
    
    ApplicationContext applicationContext

    def injectedMethods = {
        def self = this
        
        controller { clazz ->
            
            def pluginName = PluginUtils.getNameOfDefiningPlugin(applicationContext, clazz)

            displayMessage { String msg ->
                self.displayMessage(msg, delegate.request, pluginName)
            }
            displayMessage { Map args ->
                self.displayMessage(args, delegate.request, pluginName)
            }
            displayFlashMessage { String msg ->
                self.displayFlashMessage(msg, delegate.flash, pluginName)
            }
            displayFlashMessage { Map args ->
                self.displayFlashMessage(args, delegate.flash, pluginName)
            }

            if (pluginName) {
                getPluginSession() { ->
                    self.getPluginSession(pluginName)
                }
                getPluginFlash() { ->
                    self.getPluginFlash(pluginName)
                }
                getPluginRequestAttributes() { ->
                    self.getPluginRequestAttributes(pluginName)
                }
            }
        }
    }

    PropertyNamespacer getPluginSession(String pluginName) {
        def req = RCH.requestAttributes.currentRequest
        def wrapper = req[SESSION_WRAPPER_KEY]
        if (!wrapper) {
            def session = RCH.requestAttributes.session
            wrapper = new PropertyNamespacer(pluginName+'.', session, 'getAttributeNames')
            req[SESSION_WRAPPER_KEY] = wrapper
        } 
        return wrapper
    }
    
    PropertyNamespacer getPluginFlash(String pluginName) {
        def req = RCH.requestAttributes.currentRequest
        def wrapper = req[FLASH_WRAPPER_KEY]
        if (!wrapper) {
            def flash = RCH.requestAttributes.flashScope
            wrapper = new PropertyNamespacer(pluginName+'.', flash, 'keySet')
            req[FLASH_WRAPPER_KEY] = wrapper
        } 
        return wrapper
    }
    
    PropertyNamespacer getPluginRequestAttributes(String pluginName) {
        def req = RCH.requestAttributes.currentRequest
        def wrapper = req[REQUEST_WRAPPER_KEY]
        if (!wrapper) {
            wrapper = new PropertyNamespacer(pluginName+'.', req, 'getAttributeNames')
            req[REQUEST_WRAPPER_KEY] = wrapper
        } 
        return wrapper
    }
    
    Map getDisplayMessage(scope) {
        if (log.debugEnabled) {
            log.debug "Getting display message from scope: ${scope}"
        }
        def args = [:]
        if (scope[UiConstants.DISPLAY_MESSAGE]) {
            args.text = scope[UiConstants.DISPLAY_MESSAGE]
            args.args = scope[UiConstants.DISPLAY_MESSAGE_ARGS]
            args.type = scope[UiConstants.DISPLAY_MESSAGE_TYPE]
        }
        if (log.debugEnabled) {
            log.debug "Found display message from scope [${scope}]: ${args}"
        }
        return args
    }
    
    void displayMessage(String text, request = RCH.requestAttributes.request, String pluginName = null) {
        if (log.debugEnabled) {
            log.debug "Setting display message text: ${text}"
        }
        request[UiConstants.DISPLAY_MESSAGE] = pluginName ? "plugin.${pluginName}.${text}" : text
    }

    void displayMessage(Map args, request = RCH.requestAttributes.request, String pluginName = null) {
        if (log.debugEnabled) {
            log.debug "Setting display message args: ${args}"
        }
        request[UiConstants.DISPLAY_MESSAGE] = pluginName ? "plugin.${pluginName}.${args.text}" : args.text
        request[UiConstants.DISPLAY_MESSAGE_ARGS] = args.args
        request[UiConstants.DISPLAY_MESSAGE_TYPE] = args.type
    }

    void displayFlashMessage(String text, flash = RCH.requestAttributes.flashScope, String pluginName = null) {
        if (log.debugEnabled) {
            log.debug "Setting display flash message text: ${text}"
        }
        flash[UiConstants.DISPLAY_MESSAGE] = pluginName ? "plugin.${pluginName}.${text}" : text
    }

    void displayFlashMessage(Map args, flash = RCH.requestAttributes.flashScope, String pluginName = null) {
        if (log.debugEnabled) {
            log.debug "Setting display flash message args: ${args}"
        }
        flash[UiConstants.DISPLAY_MESSAGE] = pluginName ? "plugin.${pluginName}.${args.text}" : args.text
        flash[UiConstants.DISPLAY_MESSAGE_ARGS] = args.args
        flash[UiConstants.DISPLAY_MESSAGE_TYPE] = args.type
    }
}