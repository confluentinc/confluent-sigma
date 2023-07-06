import React from 'react'
import { useParams } from "react-router-dom";

import PageTitle from "../../components/PageTitle/PageTitle";

const SigmaRuleEditor = props => {
    const title =
    (props.location && props.location.title) || {};
    console.log(props.location.title);

    return (
    <>
    <PageTitle title="Sigma Rule Editor" />

    <div>
        Title: {title}
    </div>
    </>
    );
}

export default SigmaRuleEditor;