/*??
 * COPYRIGHT (C) 2011 CHERIMOIA LLC. ALL RIGHTS RESERVED.
 *
 * THIS IS FREE SOFTWARE; YOU CAN REDISTRIBUTE IT AND/OR
 * MODIFY IT UNDER THE TERMS OF THE APACHE LICENSE,
 * VERSION 2.0 (THE "LICENSE").
 *
 * THIS LIBRARY IS DISTRIBUTED IN THE HOPE THAT IT WILL BE USEFUL,
 * BUT WITHOUT ANY WARRANTY; WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SEE THE LICENSE FOR THE SPECIFIC LANGUAGE GOVERNING PERMISSIONS
 * AND LIMITATIONS UNDER THE LICENSE.
 *
 * You should have received a copy of the Apache License
 * along with this distribution; if not, you may obtain a copy of the
 * License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 ??*/

package com.zotoh.scaladb

import org.slf4j.{LoggerFactory}
 
/**
 * @author kenl
 */
trait Loggable {
 
  	private lazy val logr = LoggerFactory.getLogger(getClass)
 
  	protected def debug(msg: => AnyRef, t: => Throwable = null) : Unit = {
		  if (logr.isDebugEnabled) {
      		if (t != null) {
        		logr.debug(msg.toString, t);
      		} else {
        		logr.debug(msg.toString)
      		}
    	}
  	}

  	protected def warn(msg: => AnyRef, t: => Throwable = null) : Unit = {
		if (logr.isWarnEnabled) {
      		if (t != null) {
        		logr.warn(msg.toString, t);
      		} else {
        		logr.warn(msg.toString)
      		}
    	}
  	}

  	protected def error(msg: => AnyRef, t: => Throwable = null) : Unit = {
      if (t != null) {
        logr.error(msg.toString, t);
      } else {
        logr.error(msg.toString)
      }
  	}

  	/*
  	*/
  	protected def info(msg: => AnyRef, t: => Throwable = null) : Unit = {
		if (logr.isInfoEnabled) {
      		if (t != null) {
        		logr.info(msg.toString, t);
      		} else {
        		logr.info(msg.toString)
      		}
    	}
  	}
 

}
