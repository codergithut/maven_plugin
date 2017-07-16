package maven_plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import com.sun.deploy.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * 
 * @phase process-sources
 */
public class CountMojo
    extends AbstractMojo
{
    private static final String[] INCLUDES_DEFAULT = { "java", "xml", "properties"};

    private File basedir;

    private File sourceDirectory;

    private File testSourceDirector;

    private List<Resource> resources;

    private List<Resource> testResources;

    private String[] includes;

    public void execute() throws MojoExecutionException {
        if( includes == null || includes.length == 0 ) {
            includes = INCLUDES_DEFAULT;
        }

        try {
            countDir(sourceDirectory);
            countDir(testSourceDirector);

            for(Resource resource : resources) {
                countDir(new File(resource.getDataFile().getPath()));
            }

            for(Resource resource : testResources) {
                countDir(new File(resource.getDataFile().getPath()));
            }
        } catch ( IOException e) {
            throw new MojoExecutionException("Unable to count lines of code.", e);
        }
    }

    private void countDir(File dir) throws IOException {
        if( !dir.exists()) {
            return;
        }

        List<File> collected = new ArrayList<File>();

        collectFiles(collected, dir);

        int lines = 0;

        for( File sourceFile : collected) {
            lines += countLines(sourceFile);
        }

        String path = dir.getAbsolutePath().substring(basedir.getAbsolutePath().length());

        getLog().info(path + ":" + lines + "lines of code in" + collected.size() + "files");
    }

    private void collectFiles(List<File> collected, File file) {
        if(file.isFile()) {
            for(String include : includes) {
                if(file.getName().endsWith("." + include)) {
                    collected.add(file);
                    break;
                }
            }
        } else {
            for(File sub : file.listFiles()) {
                collectFiles(collected, sub);
            }
        }
    }

    private int countLines(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        int line = 0;

        try {
            while(reader.ready()) {
                reader.readLine();
                line++;
            }
        } finally {
            reader.close();
            return line;
        }
    }

}
