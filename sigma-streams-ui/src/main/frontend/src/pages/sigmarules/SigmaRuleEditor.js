import React, { useState, useEffect, useRef } from "react";
import { Link, useLocation } from "react-router-dom";
import { Grid, Button } from "@material-ui/core";
import Editor from "@monaco-editor/react";
import YAML from 'yaml'
import { ToastContainer, toast } from "react-toastify";

// styles
import useStyles from "./styles";

// components
import PageTitle from "../../components/PageTitle/PageTitle";
import Notification from "../../components/Notification";

export default function SigmaRuleEditor(props) {
    var classes = useStyles();
    const inputRef = useRef(null);

    const SERVER_ENDPOINT = process.env.REACT_APP_SERVER_ENDPOINT;
    const location = useLocation();
    const title = location.state?.title;
    console.log("title is " + title);
    const [code, setCode] = useState("");
    const [modifiedCode, setModifiedCode] = useState("");

    const options = {
        //selectOnLineNumbers: true,
        //roundedSelection: false,
        //readOnly: true,
        //cursorStyle: "line",
        //automaticLayout: false,
        "codeLens": false,
        dragAndDrop: true
      };
    
    const getRule = async () => {
        if (!title) return;

        try {
            const rule = await (await fetch(SERVER_ENDPOINT +  `sigmaRule/${title}`)).json()
            console.log("Rule: " + JSON.stringify(rule));
            console.log("Rule parse: " + YAML.parse(rule));
            console.log("Rule stringify: " + YAML.stringify(rule));
            console.log("Rule: " + YAML.stringify(rule));
            setCode(YAML.stringify(rule));
            setModifiedCode(YAML.stringify(rule));
        } catch (err) {
            console.log(err.message)
        }
    }

    useEffect(() => { getRule() }, [title]);

    function handleEditorChange(value, event) {
        console.log("here is the current model value:", value);
        setModifiedCode(value);
    }

    const handleClick = () => {
        // 👇️ open file input box on click of another element
        inputRef.current.click();
    };
    
    const handleRuleChange = async () => {
        console.log("publishing rule: " + code);

        fetch(SERVER_ENDPOINT + `addSigmaRule`, {
            method: 'POST',
            body: modifiedCode
        }).then(function(response) {
            console.log(response)
            if (response.ok) {
                console.log("rule successfully submitted");
                var componentProps = {
                    type: "shipped",
                    message: "Rule was submitted",
                    variant: "contained",
                    color: "success",
                  };

                sendNotification(componentProps, {
                    type: "success",
                    position: toast.POSITION.TOP_RIGHT,
                    progressClassName: classes.progress,
                    className: classes.notification,
                });
              
            } else {
                console.log("error submitting rule");
                var componentProps = {
                    type: "defence",
                    message: "Rule was NOT submitted!",
                    variant: "contained",
                    color: "error",
                  };

                sendNotification(componentProps, {
                    type: "error",
                    position: toast.POSITION.TOP_RIGHT,
                    progressClassName: classes.progress,
                    className: classes.notification,
                });
            }
          })
          console.log('Form submitted.')
    }

    function sendNotification(componentProps, options) {
        return toast(
          <Notification
            {...componentProps}
            className={classes.notificationComponent}
          />,
          options,
        );
      }

    function handleOnDrop(event) {
        console.log("drop: " + event);
    }

    function handleDragOver(event) {
        event.preventDefault();
        console.log("drag over");
    }

    const openFile = (e) => {
        const files = e.target.files;
        var reader = new FileReader();
        reader.readAsText(files[0], "UTF-8");
    
  
        reader.onload = function (re) {
          console.log(re.target.result);
          setCode(re.target.result.replaceAll("\t", ""));
          setModifiedCode(re.target.result.replaceAll("\t", ""));
        };
      };
  
  
    return (
        <>
        <PageTitle title="Sigma Rule Editor" />
        <Grid container spacing={4}>
            <ToastContainer
                className={classes.toastsContainer}
                closeOnClick={false}
                progressClassName={classes.notificationProgress}
            />
            <Grid item xs={8}>
            <div className={classes.ruleViewer} onDrop={handleOnDrop} onDragOver={handleDragOver}>
                <Editor
                    height="90vh"
                    defaultLanguage="yaml"
                    defaultValue=""
                    value={code}
                    onChange={handleEditorChange}
                    theme="vs-dark"
                    options={options}
                    />
            </div>
            </Grid>
            <Grid item xs={4}>
                <Grid container spacing={1}>
                    <Grid item xs={12}>
                        { !title &&
                            <div>
                                <input 
                                    type="file" 
                                    ref={inputRef}
                                    onChange={openFile} 
                                    style={{ display: 'none' }}
                                />
                                <Button 
                                    className={classes.button}
                                    onClick={handleClick}>
                                    Open File
                                </Button>
                            </div>
                        }               
                    </Grid>
                    <Grid item xs={12}>
                        <Link to="/app/sigmarules">
                            <Button className={classes.button}>
                                Cancel Changes
                            </Button>
                        </Link>
                    </Grid>
                    <Grid item xs={12}> 
                        <Button 
                            className={classes.button}
                            onClick={handleRuleChange}
                            disabled={!modifiedCode}>
                            Publish Changes
                        </Button>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
        </>
    );
  }
  