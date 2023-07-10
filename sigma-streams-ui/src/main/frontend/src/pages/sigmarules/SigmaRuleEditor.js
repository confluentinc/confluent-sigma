import React, { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { Grid } from "@material-ui/core";
import Widget from "../../components/Widget";
import AceEditor from "react-ace";
import YAML from 'yaml'

import "ace-builds/src-noconflict/mode-yaml";
import "ace-builds/src-noconflict/theme-monokai";
import 'ace-builds/src-noconflict/ace';

// styles
import useStyles from "./styles";

import PageTitle from "../../components/PageTitle/PageTitle";


// Update with https://github.com/remcohaszing/monaco-yaml

export default function SigmaRuleEditor(props) {
    var classes = useStyles();

    const location = useLocation();
    console.log(props, " props");
    console.log(location, " useLocation Hook");
    const title = location.state?.title;

    const [code, setCode] = useState("");

    const getRule = async () => {
        try {
            const rule = await (await fetch(`http://localhost:8080/sigmaRule/${title}`)).json()
            console.log("Rule: " + JSON.stringify(rule));
            console.log("Rule parse: " + YAML.parse(rule));
            console.log("Rule stringify: " + YAML.stringify(rule));
            console.log("Rule: " + YAML.stringify(rule));
            setCode(YAML.stringify(rule));
        } catch (err) {
            console.log(err.message)
        }
    }

    useEffect(() => { getRule() }, []);

    const handleChange = (e) => {
      const files = e.target.files;
      var reader = new FileReader();
      reader.readAsText(files[0], "UTF-8");
  

      reader.onload = function (re) {
        console.log(re.target.result);
        setCode(re.target.result.replaceAll("\t", ""));
      };
    };
  
    function onChange(newValue) {
      setCode(newValue.replaceAll("\t", ""));
    }

    function onValidate(annotations) {
        console.log("onValidate", annotations);
    }
  
    return (
        <>
        <PageTitle title="Sigma Rule Editor" />
        <Grid container spacing={4}>
            <Grid item xs={8}>
            <div className={classes.ruleViewer}>
                <input type="file" onChange={handleChange}></input>
                <AceEditor
                width="100%"
                mode="yaml"
                theme="monokai"
                value={code}
                onChange={onChange}
                //onValidate={onValidate}
                showGutter={true}
                name="my_yaml"
                editorProps={{ $blockScrolling: true }}
                setOptions={{ useWorker: false }}
                    />
            </div>
            </Grid>
        </Grid>
        </>
    );
  }
  