/*
 * Copyright (c) 2023, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#ifndef SHARE_GC_G1_G1YOUNGGCPREEVACUATETASKS_HPP
#define SHARE_GC_G1_G1YOUNGGCPREEVACUATETASKS_HPP

#include "gc/g1/g1BatchedTask.hpp"

// Set of pre evacuate collection set tasks containing ("s" means serial):
// - Retire TLAB and Flush Logs (Java threads)
// - Flush pin count cache (Java threads)
// - Flush Logs (s) (Non-Java threads)
class G1PreEvacuateCollectionSetBatchTask : public G1BatchedTask {
  class JavaThreadRetireTLABAndFlushLogs;
  class NonJavaThreadFlushLogs;

  size_t _old_pending_cards;

  // References to the tasks to retain access to statistics.
  JavaThreadRetireTLABAndFlushLogs* _java_retire_task;
  NonJavaThreadFlushLogs* _non_java_retire_task;

public:
  G1PreEvacuateCollectionSetBatchTask();
  ~G1PreEvacuateCollectionSetBatchTask();
};

#endif // SHARE_GC_G1_G1YOUNGGCPREEVACUATETASKS_HPP
