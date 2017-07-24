/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.gettingstartedjava.helloworld;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner.TransactionCallable;

@SuppressWarnings("serial")
@WebServlet(name = "users", value = "/users" )
public class UsersServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter out = resp.getWriter();
    out.println("Users:");
  
    Spanner spanner = createSpannerService();
    
    DatabaseClient dbClient = createDbClient(spanner);
    
    spannerWriteTest(dbClient);
    
    spannerReadTest(dbClient, out);
    
    // Closes the client which will free up the resources used
    try {
      spanner.closeAsync()
          .get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

  }
  
  private Spanner createSpannerService() {
    SpannerOptions options = SpannerOptions.newBuilder()
        .build();
    Spanner spanner = options.getService();
    return spanner;
  }
  
  private DatabaseClient createDbClient(Spanner spanner) {
    SpannerOptions options = spanner.getOptions();
    String instance = "donnelson-dev-testing";
    String database = "donnelson-db";

    // Creates a database client
    DatabaseClient dbClient = spanner.getDatabaseClient(DatabaseId.of(options.getProjectId(), instance, database));

    return dbClient;
  }
  
  private void spannerWriteTest(DatabaseClient dbClient) {
    dbClient.readWriteTransaction()
        .run(new TransactionCallable<Void>() {
          @Override
          public Void run(TransactionContext transaction) throws Exception {
            transaction.buffer(Mutation.newUpdateBuilder("users")
                .set("id")
                .to(1)
                .set("name")
                .to("Landan")
                .set("age")
                .to(41)
                .build());
            return null;
          }
        });
  }
  
  private void spannerReadTest(DatabaseClient dbClient, PrintWriter out) {
    // Queries the database
    ResultSet resultSet = dbClient.singleUse()
        .executeQuery(Statement.of("SELECT name from users;"));

    // Prints the results
    while (resultSet.next()) {
      out.print("" + resultSet.getString(0));
    }
  }

}
