/*
 * Copyright 2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.scala.net

// Scala api for java.net.URI

object uri {

  type URI = java.net.URI

  class URIOps(val u: URI) {

    // URI terminology reference: http://docs.oracle.com/javase/6/docs/api/java/net/URI.html

    def authority    : String = u.getAuthority
    def fragment     : String = u.getFragment
    def host         : String = u.getHost
    def path         : String = u.getPath
    def port         : Int    = u.getPort
    def query        : String = u.getQuery
    def rawAuthority : String = u.getRawAuthority
    def rawFragment  : String = u.getRawFragment
    def rawPath      : String = u.getRawPath
    def rawQuery     : String = u.getRawQuery
    def rawSSP       : String = u.getRawSchemeSpecificPart
    def rawUserInfo  : String = u.getRawUserInfo
    def scheme       : String = u.getScheme
    def ssp          : String = u.getSchemeSpecificPart
    def userInfo     : String = u.getUserInfo

    // Aliases
    def schemeSpecificPart    : String = ssp
    def rawSchemeSpecificPart : String = rawSSP

    def withScheme    (x: String) = new URI(x,      ssp, fragment)
    def withSSP       (x: String) = new URI(scheme, x,   fragment)
    def withFragment  (x: String) = new URI(scheme, ssp, x)
    // Hierarchical
    def withAuthority (x: String) = new URI(scheme, x,         path, query, fragment)
    def withPath      (x: String) = new URI(scheme, authority, x,    query, fragment)
    def withQuery     (x: String) = new URI(scheme, authority, path, x,     fragment)
    // Hierarchical with server-based authority
    def withUserInfo  (x: String) = new URI(scheme, x,        host, port, path, query, fragment)
    def withHost      (x: String) = new URI(scheme, userInfo, x,    port, path, query, fragment)
    def withPort      (x: Int)    = new URI(scheme, userInfo, host, x,    path, query, fragment)

    def withScheme    (f: String => String) = new URI(f(scheme), ssp,    fragment)
    def withSSP       (f: String => String) = new URI(scheme,    f(ssp), fragment)
    def withFragment  (f: String => String) = new URI(scheme,    ssp,    f(fragment))
    // Hierarchical
    def withAuthority (f: String => String) = new URI(scheme,    f(authority), path,    query,    fragment)
    def withPath      (f: String => String) = new URI(scheme,    authority,    f(path), query,    fragment)
    def withQuery     (f: String => String) = new URI(scheme,    authority,    path,    f(query), fragment)
    // Hierarchical with server-based authority
    def withUserInfo  (f: String => String) = new URI(scheme,    f(userInfo), host,    port,    path, query, fragment)
    def withHost      (f: String => String) = new URI(scheme,    userInfo,    f(host), port,    path, query, fragment)
    def withPort      (f: Int    => Int)    = new URI(scheme,    userInfo,    host,    f(port), path, query, fragment)

    // Aliases
    def withSchemeSpecificPart (x: String)           : URI = withSchemeSpecificPart(x)
    def withSchemeSpecificPart (f: String => String) : URI = withSchemeSpecificPart(f)

  }
  implicit def URIOps(u: URI) = new URIOps(u)

  implicit val uriOrdering: Ordering[URI] = Ordering.by(_.toString)

}
