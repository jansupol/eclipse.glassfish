/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.oracle.javaee7.samples.batch.chunk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import jakarta.annotation.PostConstruct;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.JobExecution;
import javax.ejb.Stateless;
import javax.ejb.EJB;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

@TransactionManagement(TransactionManagementType.BEAN)
@Stateless
public class SlessBean
    implements Sless {

    boolean ejbCreateCalled = false;

    public void ejbCreate() {
        this.ejbCreateCalled = true;
    }

    public long submitJob() {
	try {
	  JobOperator jobOperator = BatchRuntime.getJobOperator();

	  Properties props = new Properties();
	  for (int i=0; i<9; i++)
		props.put("Chunk-Key-"+ i, "Chunk-Value-" + i);
	  return  jobOperator.start("ChunkJob", props);
        } catch (Exception ex) {
	  throw new RuntimeException(ex);
	}
    }

    public String getJobExitStatus(long executionId) {
	try {
	  JobOperator jobOperator = BatchRuntime.getJobOperator();
	  JobExecution je =  jobOperator.getJobExecution(executionId);
	  return je.getExitStatus();
	} catch (Exception ex) {
	  throw new RuntimeException(ex);
	}

    }

    public boolean wasEjbCreateCalled() {
	return ejbCreateCalled;
    }

														            
}
